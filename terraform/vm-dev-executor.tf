resource "google_compute_instance" "vm_dev_executor" {
  name         = "polymorphia-dev-executor-vm"
  machine_type = "e2-micro"
  zone         = var.zone_europe
  tags         = ["ssh-enabled"]

  scheduling {
    preemptible        = true
    automatic_restart  = false
    provisioning_model = "SPOT"
  }

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-12"
      size  = 10
      type  = "pd-balanced"
    }
  }

  network_interface {
    subnetwork = google_compute_subnetwork.subnet_europe.id
    access_config {
      network_tier = "STANDARD"
    }
  }

  metadata = {
    ssh-keys                = "${var.ssh_user}:${var.ssh_public_key}"
    cloudflare-tunnel-token = var.cloudflare_tunnel_token
    startup-script          = file("${path.module}/scripts/startup-vm3-executor.sh")
  }

  service_account {
    scopes = ["cloud-platform"]
  }
}
