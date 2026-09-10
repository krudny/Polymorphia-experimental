resource "google_compute_network" "vpc_network" {
  name                    = "polymorphia-vpc"
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "subnet_us" {
  name          = "subnet-us-central1"
  ip_cidr_range = "10.10.0.0/24"
  region        = var.region_us
  network       = google_compute_network.vpc_network.id
}

resource "google_compute_subnetwork" "subnet_europe" {
  name          = "subnet-europe-west1"
  ip_cidr_range = "10.20.0.0/24"
  region        = var.region_europe
  network       = google_compute_network.vpc_network.id
}
