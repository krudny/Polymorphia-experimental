import {
  InviteRequestDTO,
  RegisterRequestDTO,
  Role,
  Roles,
  StudentDetailsDTOWithType,
  UserDetailsDTO,
} from "@/interfaces/api/user";
import {
  CreateAnimalRequestDTO,
  StudentCourseGroupAssignmentIdResponseDTO,
  StudentProfileResponseDTO,
} from "@/interfaces/api/student";
import { ApiClient } from "@/services/api/client";

const UserService = {
  getStudentProfile: async (
    courseId: number
  ): Promise<StudentProfileResponseDTO> => {
    return await ApiClient.get<StudentProfileResponseDTO>(
      `/students/profile?courseId=${courseId}`
    );
  },
  hasValidAnimalInCourse: async (courseId: number): Promise<boolean> => {
    return await ApiClient.get<boolean>(
      `/students/animals/is-valid?courseId=${courseId}`
    );
  },
  getCourseGroup: async (
    courseId: number
  ): Promise<StudentCourseGroupAssignmentIdResponseDTO> => {
    return await ApiClient.get<StudentCourseGroupAssignmentIdResponseDTO>(
      `/students/course-group?courseId=${courseId}`
    );
  },
  createAnimal: async (request: CreateAnimalRequestDTO): Promise<void> => {
    await ApiClient.post("/students/animals", request);
  },
  deleteAnimal: async (animalId: number): Promise<void> => {
    await ApiClient.delete(`/students/${animalId}`);
  },
  getUserRole: async (): Promise<Role> => {
    return await ApiClient.get<Role>("/users/role");
  },
  getCurrentUser: async (): Promise<UserDetailsDTO> => {
    return await ApiClient.get<UserDetailsDTO>("/users/context");
  },
  setUserPreferredCourse: async (courseId: number): Promise<void> => {
    await ApiClient.post(`/users/preferred-course?courseId=${courseId}`);
  },

  getRandomUsers: async (): Promise<StudentDetailsDTOWithType[]> => {
    return [
      {
        userRole: Roles.STUDENT,
        userDetails: {
          id: 1,
          fullName: "Kamil Rudny",
          animalName: "Gerard Pocieszny",
          evolutionStage: "Majestatyczna bestia",
          group: "BM-15-00",
          imageUrl: "images/evolution-stages/4.jpg",
          position: 1,
          courseId: 1,
        },
      },
      {
        userRole: Roles.STUDENT,
        userDetails: {
          id: 2,
          fullName: "Kamil Śmieszny",
          animalName: "Gerard Wesoły",
          evolutionStage: "Majestatyczna bestia",
          group: "BM-15-00",
          imageUrl: "images/evolution-stages/5.jpg",
          position: 2,
          courseId: 1,
        },
      },
      {
        userRole: Roles.STUDENT,
        userDetails: {
          id: 3,
          fullName: "Kamil Rudny2",
          animalName: "Gerard Pocieszny",
          evolutionStage: "Majestatyczna bestia",
          group: "BM-16-40",
          imageUrl: "images/evolution-stages/4.jpg",
          position: 1,
          courseId: 1,
        },
      },
      {
        userRole: Roles.STUDENT,
        userDetails: {
          id: 4,
          fullName: "Kamil Śmieszny2",
          animalName: "Gerard Wesoły",
          evolutionStage: "Majestatyczna bestia",
          group: "BM-16-40",
          imageUrl: "images/evolution-stages/5.jpg",
          position: 2,
          courseId: 1,
        },
      },
      {
        userRole: Roles.STUDENT,
        userDetails: {
          id: 3,
          fullName: "Kamil Rudny2",
          animalName: "Gerard Pocieszny",
          evolutionStage: "Majestatyczna bestia",
          group: "BM-20-00",
          imageUrl: "images/evolution-stages/4.jpg",
          position: 1,
          courseId: 1,
        },
      },
      {
        userRole: Roles.STUDENT,
        userDetails: {
          id: 4,
          fullName: "Kamil Śmieszny2",
          animalName: "Gerard Wesoły",
          evolutionStage: "Majestatyczna bestia",
          group: "BM-20-00",
          imageUrl: "images/evolution-stages/5.jpg",
          position: 2,
          courseId: 1,
        },
      },
    ];
  },

  inviteUser: async (request: InviteRequestDTO): Promise<number> => {
    return await ApiClient.post("/invitation/course", request);
  },

  register: async (request: RegisterRequestDTO): Promise<void> => {
    await ApiClient.post("/invitation/register-user", request);
  },
};

export default UserService;
