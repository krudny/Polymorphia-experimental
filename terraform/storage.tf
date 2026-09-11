resource "google_storage_bucket" "seed_bucket" {
  name                     = "polymorphia-db-seeds-${var.project_id}"
  location                 = "us-central1"
  storage_class            = "STANDARD"
  public_access_prevention = "enforced"
  uniform_bucket_level_access = true

  versioning {
    enabled = false # Wyłączone, by nie płacić za stare wersje nadpisanych plików
  }
}

resource "google_storage_bucket_iam_member" "sa_storage_reader" {
  bucket = google_storage_bucket.seed_bucket.name
  role   = "roles/storage.objectViewer"
  member = "serviceAccount:github-actions-sa@${var.project_id}.iam.gserviceaccount.com"
}

output "seed_bucket_name" {
  value       = google_storage_bucket.seed_bucket.name
  description = "Nazwa bucketa na zrzuty bazy danych (seedy)"
}