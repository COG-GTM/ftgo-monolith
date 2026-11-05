#!/usr/bin/env python3
"""Local mixed HTTP benchmark; Python standard library only. Creates persistent test data."""
import argparse
import concurrent.futures
import http.client
import json
import math
import pathlib
import threading
import time
import urllib.parse


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default="http://localhost:8081")
    parser.add_argument("--concurrency", type=int, required=True)
    parser.add_argument("--warmup", type=float, default=15)
    parser.add_argument("--duration", type=float, default=30)
    parser.add_argument("--seed", type=pathlib.Path, required=True)
    parser.add_argument("--label", required=True)
    args = parser.parse_args()
    if args.concurrency < 1 or args.duration <= 0 or args.warmup < 0:
        parser.error("positive concurrency/duration and nonnegative warmup required")
    url = urllib.parse.urlsplit(args.url)
    if url.scheme != "http" or url.path not in ("", "/"):
        parser.error("use an http://host:port URL without a path")

    def connect():
        return http.client.HTTPConnection(url.hostname, url.port or 80, timeout=30)

    def create(path, body):
        connection = connect()
        try:
            connection.request("POST", path, json.dumps(body), {"Content-Type": "application/json"})
            response = connection.getresponse()
            payload = response.read()
            if response.status != 200:
                raise RuntimeError(f"Seed {path}: HTTP {response.status}: {payload!r}")
            return json.loads(payload)
        finally:
            connection.close()

    if args.seed.exists():
        seed = json.loads(args.seed.read_text())
    else:
        consumer = create("/consumers", {"name": {"firstName": "Load", "lastName": "Trial"}})["consumerId"]
        restaurant = create("/restaurants", {
            "name": "Load Trial", "address": {"street1": "1 Main St", "city": "Oakland", "state": "CA", "zip": "94612"},
            "menu": {"menuItemDTOs": [{"id": "1", "name": "Lunch", "price": "12.34"}]}})["id"]
        body = {"consumerId": consumer, "restaurantId": restaurant, "lineItems": [{"menuItemId": "1", "quantity": 1}]}
        seed = {"consumer": consumer, "restaurant": restaurant, "order": create("/orders", body)["orderId"], "body": body}
        args.seed.write_text(json.dumps(seed, indent=2) + "\n")
    routes = [("GET", f'/consumers/{seed["consumer"]}', None),
              ("GET", f'/restaurants/{seed["restaurant"]}', None),
              ("GET", f'/orders/{seed["order"]}', None),
              ("POST", "/orders", json.dumps(seed["body"]))]
    barrier = threading.Barrier(args.concurrency + 1)
    timing = {}

    def worker(index):
        connection = connect()
        samples, errors, counts, warmup_errors = [], {}, [0] * 4, 0
        barrier.wait()
        iteration = index
        try:
            while time.monotonic() < timing["end"]:
                route = iteration % 4
                method, path, body = routes[route]
                started = time.monotonic()
                measured = started >= timing["measure"]
                error = None
                try:
                    connection.request(method, path, body, {"Content-Type": "application/json"})
                    response = connection.getresponse()
                    response.read()
                    if response.status != 200:
                        error = f"HTTP {response.status}"
                except (OSError, http.client.HTTPException) as exc:
                    error = type(exc).__name__
                    connection.close()
                    connection = connect()
                if measured:
                    samples.append((time.monotonic() - started) * 1000)
                    counts[route] += 1
                    if error:
                        errors[error] = errors.get(error, 0) + 1
                elif error:
                    warmup_errors += 1
                iteration += 1
        finally:
            connection.close()
        return samples, errors, counts, warmup_errors

    with concurrent.futures.ThreadPoolExecutor(max_workers=args.concurrency) as pool:
        futures = [pool.submit(worker, index) for index in range(args.concurrency)]
        timing["measure"] = time.monotonic() + args.warmup
        timing["end"] = timing["measure"] + args.duration
        barrier.wait()
        results = [future.result() for future in futures]
    elapsed = time.monotonic() - timing["measure"]
    samples = sorted(sample for result in results for sample in result[0])
    errors = {}
    for result in results:
        for error, count in result[1].items():
            errors[error] = errors.get(error, 0) + count

    def percentile(p):
        return round(samples[max(0, math.ceil(len(samples) * p) - 1)], 3) if samples else None

    print(json.dumps({"label": args.label, "concurrency": args.concurrency,
                      "warmup_s": args.warmup, "requested_duration_s": args.duration,
                      "elapsed_s": round(elapsed, 3), "requests": len(samples),
                      "requests_per_second": round(len(samples) / elapsed, 2),
                      "p50_ms": percentile(.5), "p95_ms": percentile(.95), "p99_ms": percentile(.99),
                      "errors": errors, "warmup_errors": sum(r[3] for r in results),
                      "route_counts": [sum(r[2][i] for r in results) for i in range(4)]}), flush=True)
    if errors or any(r[3] for r in results) or not samples:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
