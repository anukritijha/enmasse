local version = std.extVar("VERSION");
local router = import "router.jsonnet";
local common = import "common.jsonnet";
{
  service(instance)::
    common.service(instance, "console", "console", "http", 8080, 8080),

  container(use_sasldb, image_repo, env)::
    {
      local mount_path = "/var/lib/qdrouterd",
      local sasldb_env = [{
          "name": "SASLDB",
          "value": mount_path + "/qdrouterd.sasldb"
        }],
      "image": image_repo + ":" + version,
      "name": "console",
      "env": env + (if use_sasldb then sasldb_env else []),
      "ports": [
        {
          "name": "http",
          "containerPort": 8080,
          "protocol": "TCP"
        },
        {
          "name": "amqp-ws",
          "containerPort": 56720,
          "protocol": "TCP"
        }
      ],
      "livenessProbe": {
        "tcpSocket": {
          "port": "http"
        }
      },
      [if use_sasldb then "volumeMounts"]: [{"name": "sasldb-vol","mountPath": mount_path}]
   },

  deployment(use_sasldb, instance, image_repo)::
    {
      "apiVersion": "extensions/v1beta1",
      "kind": "Deployment",
      "metadata": {
        "labels": {
          "name": "console",
          "app": "enmasse",
          "instance": instance
        },
        "name": "console"
      },
      "spec": {
        "replicas": 1,
        "template": {
          "metadata": {
            "labels": {
              "name": "console",
              "instance": instance,
              "app": "enmasse"
            }
          },
          "spec": {
            "containers": [
              self.container(use_sasldb, image_repo, [])
            ],
            [if use_sasldb then "volumes" ]: [router.sasldb_volume()]
          }
        }
      }
    }
}
