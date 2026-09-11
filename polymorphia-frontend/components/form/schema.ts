import * as z from "zod";
import { Roles } from "@/interfaces/api/user";

const passwordValidation = z
  .string()
  .min(8, "Hasło musi mieć co najmniej 8 znaków")
  .max(256, "Hasło może mieć maksymalnie 256 znaków")
  .regex(/[A-Z]/, "Hasło musi zawierać co najmniej jedną wielką literę")
  .regex(/[a-z]/, "Hasło musi zawierać co najmniej jedną małą literę")
  .regex(/[0-9]/, "Hasło musi zawierać co najmniej jedną cyfrę")
  .regex(
    /[^A-Za-z0-9]/,
    "Hasło musi zawierać co najmniej jeden znak specjalny"
  );

export const emailField = z.string().email("Nieprawidłowy email");

export const loginSchema = z.object({
  email: emailField,
  password: z.string(),
});

export const changePasswordSchema = z
  .object({
    oldPassword: z.string().min(1, "Stare hasło jest wymagane"),
    newPassword: passwordValidation,
    confirmNewPassword: z.string(),
  })
  .refine((data) => data.newPassword === data.confirmNewPassword, {
    message: "Hasła nie są zgodne",
    path: ["confirmNewPassword"],
  });

export const registerSchema = z.object({
  password: passwordValidation,
});

export const inviteSchema = z.object({
  firstName: z.string().nonempty(),
  lastName: z.string().nonempty(),
  role: z.nativeEnum(Roles),
  email: emailField,
  courseId: z.number(),
});

export const createAnimalSchema = z.object({
  animalName: z.string().nonempty(),
});

export const resetPasswordSchema = z
  .object({
    newPassword: passwordValidation,
    confirmNewPassword: z.string(),
  })
  .refine((data) => data.newPassword === data.confirmNewPassword, {
    message: "Hasła nie są zgodne",
    path: ["confirmNewPassword"],
  });

export const forgotPasswordSchema = z.object({
  email: emailField,
});
