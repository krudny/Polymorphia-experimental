import { SpeedDialModalProps } from "@/components/speed-dial/modals/types";
import Modal from "@/components/modal";
import "./index.css";
import React, { FormEvent, useState } from "react";
import ButtonWithBorder from "@/components/button";
import { useForm } from "@tanstack/react-form";
import { inviteSchema } from "@/components/form/schema";
import {
  InviteRequestDTO,
  Role,
  Roles,
  RoleTextMap,
} from "@/interfaces/api/user";
import useInviteUser from "@/hooks/course/invitation/useInviteStudent";
import Selector from "@/components/selector";
import { useUserDetails } from "@/hooks/contexts/useUserContext";
import { isValidRole } from "@/components/speed-dial/modals/invite-user/invite-to-course/utils/isValidRole";
import { FieldErrorMessage } from "@/components/form/FieldErrorMessage";
import { InviteUserToCourseModalProps } from "@/components/speed-dial/modals/invite-user/invite-to-course/types";

function InviteUserToCourseModalContent({
  onEntityCreated,
}: InviteUserToCourseModalProps) {
  const { mutation } = useInviteUser();
  const { courseId } = useUserDetails();
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);

  const form = useForm({
    defaultValues: {
      firstName: "",
      lastName: "",
      indexNumber: undefined,
      email: "",
      role: Roles.UNDEFINED,
      courseId: courseId,
    } as InviteRequestDTO,
    validators: {
      onSubmit: inviteSchema,
    },
    onSubmit: async ({ value }) => {
      try {
        const createdUserId = await mutation.mutateAsync({ ...value });
        onEntityCreated?.(createdUserId);
      } catch (error) {
        // pass
      }
    },
  });

  const extractIndexNumberFromEmail = (email: string): number | null => {
    const match = email.match(/^(\d+)@/);
    return match ? parseInt(match[1], 10) : null;
  };

  const handleRoleChange = (value: string) => {
    const validatedRole = isValidRole(value) ? value : Roles.UNDEFINED;
    setSelectedRole(validatedRole);
    form.setFieldValue("role", validatedRole);

    if (selectedRole === Roles.STUDENT) {
      const indexNumber = extractIndexNumberFromEmail(
        form.getFieldValue("email")
      );
      if (indexNumber !== null) {
        form.setFieldValue("indexNumber", indexNumber);
      }
    }
  };

  const rolesOptions = Object.entries(RoleTextMap)
    .filter(([key]) => key !== Roles.UNDEFINED)
    .map(([value, label]) => ({ label, value }));

  return (
    <div className="invite-to-course-wrapper">
      <form
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          form.handleSubmit();
        }}
      >
        <div className="invite-to-course-columns">
          <form.Field name="firstName">
            {(field) => (
              <div className="invite-to-course-input-wrapper">
                <input
                  type="text"
                  id={field.name}
                  placeholder="Imię"
                  value={field.state.value}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                  required
                  autoComplete="off"
                />
                <FieldErrorMessage field={field} />
              </div>
            )}
          </form.Field>

          <form.Field name="lastName">
            {(field) => (
              <div className="invite-to-course-input-wrapper">
                <input
                  type="text"
                  id={field.name}
                  placeholder="Nazwisko"
                  value={field.state.value}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                  required
                  autoComplete="off"
                />
                <FieldErrorMessage field={field} />
              </div>
            )}
          </form.Field>
        </div>

        <div className="invite-to-course-columns">
          <form.Field name="email">
            {(field) => (
              <div className="invite-to-course-input-wrapper">
                <input
                  type="email"
                  id={field.name}
                  placeholder="Email"
                  value={field.state.value}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                  required
                  autoComplete="off"
                />
                <FieldErrorMessage field={field} />
              </div>
            )}
          </form.Field>

          <div className="invite-to-course-selector">
            <Selector
              options={rolesOptions}
              value={selectedRole || ""}
              onChange={handleRoleChange}
              placeholder="Rola"
              size="3xl"
              padding="base"
              centeredPlaceholder={true}
              centeredOptions={true}
              className="!border-b-2 !border-t-0 !border-x-0 !rounded-none !w-40"
            />
          </div>
        </div>

        {selectedRole === Roles.STUDENT && (
          <form.Field name="indexNumber">
            {(field) => (
              <div className="invite-to-course-input-wrapper">
                <input
                  type="number"
                  id={field.name}
                  placeholder="Numer indeksu"
                  value={
                    field.state.value === undefined ? "" : field.state.value
                  }
                  onBlur={field.handleBlur}
                  onChange={(event) => {
                    const value = event.target.value;
                    field.handleChange(value === "" ? 0 : parseInt(value, 10));
                  }}
                  required
                  autoComplete="off"
                />
                <FieldErrorMessage field={field} />
              </div>
            )}
          </form.Field>
        )}

        <form.Subscribe
          selector={(state) => [state.canSubmit, state.isPristine]}
        >
          {([canSubmit, isPristine]) => (
            <ButtonWithBorder
              text={mutation.isPending ? "Wysyłanie..." : "Wyślij zaproszenie"}
              className="!mx-0 !py-1 !w-full"
              isActive={!isPristine && canSubmit && !mutation.isPending}
            />
          )}
        </form.Subscribe>
      </form>
    </div>
  );
}

export default function InviteUserToCourseModal({
  onClosedAction,
  onEntityCreated,
}: SpeedDialModalProps & InviteUserToCourseModalProps) {
  return (
    <Modal
      isDataPresented={true}
      onClosed={onClosedAction}
      title="Zaproś użytkownika do kursu"
      subtitle="Uzupełnij wszystkie dane:"
    >
      <InviteUserToCourseModalContent onEntityCreated={onEntityCreated} />
    </Modal>
  );
}
