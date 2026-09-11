import { API_HOST } from "@/services/api";
import { BackendErrorResponse } from "@/interfaces/api/error";
import { ApiError } from "@/services/api/error";
import {
  ApiBody,
  ApiRequestOptions,
  HttpMethods,
  ResponseType,
} from "@/services/api/types";

const GENERIC_ERROR_MESSAGE = "Wystąpił nieoczekiwany błąd. Spróbuj ponownie.";

export const readErrorMessage = async (response: Response): Promise<string> => {
  try {
    const { detail, title } = (await response.json()) as BackendErrorResponse;
    return detail?.trim() || title?.trim() || GENERIC_ERROR_MESSAGE;
  } catch {
    return GENERIC_ERROR_MESSAGE;
  }
};

const buildUrl = (path: string): string =>
  `${API_HOST}${path.startsWith("/") ? path : `/${path}`}`;

const buildHeaders = (
  headers: HeadersInit | undefined,
  hasJsonBody: boolean
): Headers | undefined => {
  if (!headers && !hasJsonBody) {
    return undefined;
  }
  const result = new Headers(headers);
  if (hasJsonBody && !result.has("Content-Type")) {
    result.set("Content-Type", "application/json");
  }
  return result;
};

const isApiJsonBody = (body: unknown): body is ApiBody =>
  body != null &&
  typeof body === "object" &&
  !(body instanceof FormData) &&
  !(body instanceof URLSearchParams);

async function request<TResponse>({
  path,
  method,
  body,
  headers,
  responseType = ResponseType.JSON,
}: ApiRequestOptions): Promise<TResponse> {
  const hasJsonBody = isApiJsonBody(body);
  const url = buildUrl(path);
  headers = buildHeaders(headers, hasJsonBody);

  const requestInit: RequestInit = {
    method,
    credentials: "include",
  };

  if (headers) {
    requestInit.headers = headers;
  }

  if (hasJsonBody) {
    requestInit.body = JSON.stringify(body);
  } else if (body !== undefined) {
    requestInit.body = body;
  }

  const response = await fetch(url, requestInit).catch((_error) => {
    throw new ApiError("Serwer nie odpowiada.", 503);
  });

  if (!response.ok) {
    throw new ApiError(await readErrorMessage(response), response.status);
  }

  const contentLength = response.headers.get("content-length");
  if (
    response.status === 204 ||
    (contentLength && Number(contentLength) === 0)
  ) {
    return undefined as TResponse;
  }

  try {
    if (responseType === "blob") {
      return (await response.blob()) as TResponse;
    }

    return (await response.json()) as TResponse;
  } catch {
    throw new ApiError("Niepoprawna odpowiedź serwera.", 500);
  }
}

export const ApiClient = {
  get<TResponse>(path: string, headers?: HeadersInit) {
    return request<TResponse>({ path, method: HttpMethods.GET, headers });
  },

  getBlob(path: string, headers?: HeadersInit): Promise<Blob> {
    return request<Blob>({
      path,
      method: HttpMethods.GET,
      headers,
      responseType: ResponseType.BLOB,
    });
  },

  post<TResponse = void>(path: string, body?: ApiBody, headers?: HeadersInit) {
    return request<TResponse>({
      path,
      method: HttpMethods.POST,
      body,
      headers,
    });
  },

  put<TResponse = void>(path: string, body?: ApiBody, headers?: HeadersInit) {
    return request<TResponse>({ path, method: HttpMethods.PUT, body, headers });
  },

  delete(path: string, body?: ApiBody, headers?: HeadersInit): Promise<void> {
    return request({ path, method: HttpMethods.DELETE, body, headers });
  },
};
