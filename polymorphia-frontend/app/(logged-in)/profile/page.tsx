"use client";
import { ProfileProvider } from "@/providers/profile";
import ProfileView from "@/views/profile";

export default function Profile() {
  return (
    <ProfileProvider>
      <ProfileView />
    </ProfileProvider>
  );
}
