import { MenuIcon } from "lucide-react";
import { useEffect, useRef } from "react";
import MenuSection from "@/components/navigation/MenuSection";
import Line from "@/components/navigation/Line";
import { animateNavbar } from "@/animations/Navigation";
import "./index.css";
import { updateMenuItems } from "@/components/course/event-section/EventSectionUtils";
import useEventSections from "@/hooks/course/event-section/useEventSections";
import useNavigationContext from "@/hooks/contexts/useNavigationContext";
import {
  useBottomMenuItems,
  useMainMenuItems,
} from "@/hooks/app/navigation/useMenuOptions";
import useUserContext from "@/hooks/contexts/useUserContext";
import { useTitle } from "@/hooks/app/title/useTitle";
import useNotificationContext from "@/hooks/contexts/useNotificationsContext";
import { useMenuCourseOptionText } from "@/hooks/app/navigation/useMenuCourseOptionText";

export default function Navbar() {
  const { isNavbarExpanded, setIsNavbarExpanded } = useNavigationContext();
  const drawerRef = useRef<HTMLDivElement | null>(null);
  const { data: eventSections } = useEventSections();
  const { title } = useTitle();
  const { userRole } = useUserContext();
  const { notificationCount, setIsNotificationModalOpen } =
    useNotificationContext();
  const courseOptionText = useMenuCourseOptionText(userRole);

  useEffect(() => {
    const drawer = drawerRef.current;
    if (!drawer) {
      return;
    }

    animateNavbar(drawer, isNavbarExpanded);
  }, [isNavbarExpanded]);

  useEffect(() => {
    if (isNavbarExpanded) {
      document.body.style.overflow = isNavbarExpanded ? "hidden" : "auto";
    }

    return () => {
      document.body.style.overflow = "auto";
    };
  }, [isNavbarExpanded]);

  const menuItems = useMainMenuItems();

  if (eventSections) {
    updateMenuItems(menuItems, eventSections, courseOptionText, userRole);
  }

  return (
    <div className="navbar">
      <div className="navbar-visible">
        <MenuIcon
          size={38}
          onClick={() => setIsNavbarExpanded(!isNavbarExpanded)}
          className="cursor-pointer"
        />
        <h1>{title}</h1>
        <div
          className="navbar-visible-notifications"
          onClick={() => setIsNotificationModalOpen(true)}
        >
          <span>notifications</span>
          {notificationCount > 0 && (
            <span className="navbar-notification-badge" />
          )}
        </div>
      </div>
      <div ref={drawerRef} className="navbar-drawer">
        <div className="flex-1">
          <MenuSection options={menuItems} />
        </div>
        <div className="mt-3">
          <Line />
          <MenuSection options={useBottomMenuItems()} />
        </div>
      </div>
    </div>
  );
}
