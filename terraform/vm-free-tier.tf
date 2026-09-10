resource "google_compute_address" "free_tier_static_ip" {
  name   = "polymorphia-dev-staging-ip"
  region = "us-central1"
}

resource "google_compute_instance" "vm_free_tier" {
  name         = "polymorphia-dev-staging-vm"
  machine_type = "e2-micro"
  zone         = var.zone_us
  tags         = ["web-server", "direct-ssh", "ssh-enabled"]

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
      size  = 15
      type  = "pd-balanced"
    }
  }

  network_interface {
    subnetwork = google_compute_subnetwork.subnet_us.id
    access_config {
      network_tier = "STANDARD"
      nat_ip = google_compute_address.free_tier_static_ip.address
    }
  }

  metadata = {
    ssh-keys       = "${var.ssh_user}:${var.ssh_public_key}"
    startup-script = file("${path.module}/scripts/startup-vm1-free.sh")
  }

  service_account {
    scopes = ["cloud-platform"]
  }
}

output "free_tier_vm_ip" {
  value       = google_compute_instance.vm_free_tier.network_interface[0].access_config[0].nat_ip
  description = "Zewnetrzny adres IP maszyny Free Tier"
}
