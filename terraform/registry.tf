resource "google_artifact_registry_repository" "polymorphia_repo" {
  location               = var.region_europe
  repository_id          = "polymorphia"
  description            = "Docker repository for Polymorphia services"
  format                 = "DOCKER"
  cleanup_policy_dry_run = false

  cleanup_policies {
    id     = "keep-latest-versions"
    action = "KEEP"
    condition {
      tag_state    = "TAGGED"
      tag_prefixes = ["latest-production", "latest-staging", "latest-develop"]
    }
  }

  cleanup_policies {
    id     = "delete-old-tags-after-3-days"
    action = "DELETE"
    condition {
      tag_state  = "TAGGED"
      older_than = "259200s" # 3 dni
    }
  }

  cleanup_policies {
    id     = "delete-untagged-images"
    action = "DELETE"
    condition {
      tag_state  = "UNTAGGED"
      older_than = "86400s" # 1 dzień
    }
  }
}
