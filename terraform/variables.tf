variable "project_id" {
  type        = string
  default     = "polymorphia-b52b06"
  description = "Identyfikator projektu GCP"
}

variable "region_us" {
  type        = string
  default     = "us-central1"
  description = "Region dla instancji Free Tier"
}

variable "zone_us" {
  type        = string
  default     = "us-central1-c"
  description = "Strefa dla instancji Free Tier"
}

variable "region_europe" {
  type        = string
  default     = "europe-west1"
  description = "Region dla produkcji i executora"
}

variable "zone_europe" {
  type        = string
  default     = "europe-west1-b"
  description = "Strefa dla produkcji i executora"
}

variable "ssh_user" {
  type        = string
  default     = "k_rudny1"
  description = "Użytkownik do logowania przez SSH"
}

variable "ssh_public_key" {
  type        = string
  description = "Klucz publiczny SSH do autoryzacji z GitHub Actions"
}

variable "cloudflare_tunnel_token" {
  type        = string
  sensitive   = true
  description = "Token Cloudflare Tunnel dla maszyny VM 3"
}
