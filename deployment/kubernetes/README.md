# Kubernetes database credentials

Create the `ftgo-db-secret` Secret before deployment with
`./deployment/kubernetes/misc/create-db-secret.sh`. Its keys are `root-password`,
`username`, and `password`; the script reads `MYSQL_ROOT_PASSWORD`, `MYSQL_USER`
(default `mysqluser`), and `MYSQL_PASSWORD` from the environment and generates
missing passwords. It leaves an existing Secret unchanged.

The MySQL StatefulSet reads those keys through `secretKeyRef`. Application pods
must carry the label `svc: ftgo-application` and read the database username and
password from the same Secret with `secretKeyRef` so they can connect under the
NetworkPolicy.

The `ftgo-mysql` NetworkPolicy permits ingress to MySQL only from pods labelled
`svc: ftgo-application`. The cluster CNI must enforce Kubernetes NetworkPolicies
for this restriction to take effect.
