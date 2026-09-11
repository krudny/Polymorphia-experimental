import { ApiClient } from "@/services/api/client";
import {
  ExecuteRequestDTO,
  ExecuteTaskResponseDTO,
  SubmitTaskResponseDTO,
  TaskDetailsResponseDTO,
  TaskSubmissionStatusResponseDTO,
} from "@/interfaces/api/tasks/types";

const TaskService = {
  getTaskDetails: async (taskId: number): Promise<TaskDetailsResponseDTO> => {
    return ApiClient.get(`/tasks/${taskId}`);
  },

  runTask: async (
    taskId: number,
    request: ExecuteRequestDTO
  ): Promise<ExecuteTaskResponseDTO> => {
    return ApiClient.post(`/tasks/${taskId}/run`, request);
  },

  submitTask: async (
    taskId: number,
    request: ExecuteRequestDTO
  ): Promise<SubmitTaskResponseDTO> => {
    return ApiClient.post(`/tasks/${taskId}/submissions`, request);
  },

  getSubmissionStatus: async (
    taskId: number,
    submissionId: number
  ): Promise<TaskSubmissionStatusResponseDTO> => {
    return ApiClient.get(`/tasks/${taskId}/submissions/${submissionId}`);
  },
};

export default TaskService;
