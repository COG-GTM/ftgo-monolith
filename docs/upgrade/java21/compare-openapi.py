"""Compare AB-563 Swagger 2 with springdoc OpenAPI 3 (Python standard library).

Usage: python3 docs/upgrade/java21/compare-openapi.py /path/to/v3-api-docs.json
"""
import json
import pathlib
import sys


def normalize(value):
    if isinstance(value, dict):
        return {key: normalize(item) for key, item in value.items()
                if key != "title" and not (key == "additionalProperties" and item == {"type": "object"})}
    if isinstance(value, list):
        return [normalize(item) for item in value]
    if isinstance(value, str):
        return value.replace("#/definitions/", "#/components/schemas/")
    return value


def compare(baseline, current):
    methods = {"get", "put", "post", "delete", "options", "head", "patch", "trace"}
    operations = lambda spec: {(path, method) for path, item in spec["paths"].items()
                               for method in item if method in methods}
    before, after = operations(baseline), operations(current)
    assert before == after, f"Operations removed: {before - after}; added: {after - before}"
    schemas = normalize(baseline["definitions"])
    assert schemas == normalize(current["components"]["schemas"]), "Normalized schema definitions differ"
    responses = 0
    requests = 0
    for path, method in sorted(before):
        old = baseline["paths"][path][method]
        new = current["paths"][path][method]
        assert old["responses"].keys() == new["responses"].keys(), (path, method, "response codes")
        for code, response in old["responses"].items():
            if "schema" in response:
                content = new["responses"][code]["content"]
                assert all(normalize(response["schema"]) == normalize(item["schema"])
                           for item in content.values()), (path, method, code)
                responses += 1
        for parameter in old.get("parameters", []):
            if parameter["in"] == "body":
                content = new["requestBody"]["content"]
                assert all(normalize(parameter["schema"]) == normalize(item["schema"])
                           for item in content.values()), (path, method, "request")
                requests += 1
    print(f"PASS: {len(baseline['paths'])} paths, {len(before)} operations, {len(schemas)} schemas unchanged")
    print(f"PASS: {requests} request bodies and {responses} documented response schemas unchanged")


if __name__ == "__main__":
    with pathlib.Path(__file__).with_name("api-baseline.json").open() as source:
        baseline = json.load(source)
    with open(sys.argv[1]) as source:
        compare(baseline, json.load(source))
