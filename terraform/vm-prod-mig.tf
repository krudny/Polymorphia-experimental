resource "google_compute_address" "prod_static_ip" {
  name         = "polymorphia-prod-ip"
  region       = var.region_europe
  network_tier = "STANDARD"
}

resource "google_compute_disk" "prod_data_disk" {
  name  = "polymorphia-data-disk"
  type  = "pd-balanced"
  zone  = var.zone_europe
  size  = 10

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_compute_instance_template" "prod_template" {
  name_prefix  = "polymorphia-prod-template-"
  machine_type = "e2-micro"
  region       = var.region_europe
  tags         = ["web-server", "direct-ssh", "ssh-enabled"]

  scheduling {
    preemptible                 = true
    automatic_restart           = false
    provisioning_model          = "SPOT"
    instance_termination_action = "STOP"
  }

  disk {
    source_image = "debian-cloud/debian-12"
    auto_delete  = true
    boot         = true
    disk_type    = "pd-balanced"
    disk_size_gb = 10
  }

  network_interface {
    subnetwork = google_compute_subnetwork.subnet_europe.id
    access_config {
      network_tier = "STANDARD"
    }
  }

  metadata = {
    ssh-keys        = "${var.ssh_user}:${var.ssh_public_key}"
    startup-script  = file("${path.module}/scripts/startup-vm2-prod.sh")
    shutdown-script = file("${path.module}/scripts/shutdown-vm2-prod.sh")
  }

  service_account {
    scopes = ["cloud-platform"]
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "google_compute_instance_group_manager" "prod_mig" {
  name               = "polymorphia-production-mig"
  base_instance_name = "polymorphia-prod"
  zone               = var.zone_europe
  target_size        = 0

  version {
    instance_template = google_compute_instance_template.prod_template.id
  }

  update_policy {
    type                  = "OPPORTUNISTIC"
    minimal_action        = "REPLACE"
    max_surge_fixed       = 0
    max_unavailable_fixed = 1
  }

  lifecycle {
    ignore_changes = [target_size]
  }
}

resource "google_compute_per_instance_config" "prod_mig_disk_attachment" {
  zone                   = var.zone_europe
  instance_group_manager = google_compute_instance_group_manager.prod_mig.name
  name                   = "polymorphia-prod-0"
  preserved_state {
    disk {
      device_name = "polymorphia-data-disk"
      source      = google_compute_disk.prod_data_disk.id
      mode        = "READ_WRITE"
      delete_rule = "NEVER"
    }
    external_ip {
      interface_name = "nic0"
      auto_delete    = "NEVER"
      ip_address {
        address = google_compute_address.prod_static_ip.id
      }
    }
  }
}

resource "google_compute_resource_policy" "daily_data_disk_backup" {
  name   = "polymorphia-data-disk-daily-backup"
  region = var.region_europe
  snapshot_schedule_policy {
    schedule {
      daily_schedule {
        days_in_cycle = 1
        start_time    = "03:00"
      }
    }
    retention_policy {
      max_retention_days    = 7
      on_source_disk_delete = "KEEP_AUTO_SNAPSHOTS"
    }
  }
}

resource "google_compute_disk_resource_policy_attachment" "prod_disk_backup_attachment" {
  name = google_compute_resource_policy.daily_data_disk_backup.name
  disk = google_compute_disk.prod_data_disk.name
  zone = var.zone_europe
}

output "prod_vm_ip" {
  value       = google_compute_address.prod_static_ip.address
  description = "Statyczny publiczny adres IP produkcji"
}
