"use client";

import "../styles/globals.css";
import localFont from "next/font/local";
import { League_Gothic } from "next/font/google";
import { QueryClient, QueryCache, MutationCache } from "@tanstack/query-core";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactNode, useRef } from "react";
import { Toaster } from "react-hot-toast";
import toast from "react-hot-toast";
import { ThemeProvider } from "next-themes";
import { useRouter } from "next/navigation";
import BackgroundWrapper from "@/components/background-wrapper";
import { TitleProvider } from "@/providers/title/TitleContext";
import { GENERAL_APPLICATION_ROUTES } from "@/providers/title/routes";
import { ApiError } from "@/services/api/error";
import handleLogoutRedirect from "@/services/api/handle-logout-redirect";

const leagueGothic = League_Gothic({
  subsets: ["latin"],
  display: "swap",
  variable: "--font-league",
});

const materialSymbols = localFont({
  variable: "--font-family-symbols",
  style: "normal",
  src: "../public/fonts/material-symbols-subset.woff2",
  display: "block",
  weight: "100 700",
});

export default function RootLayout({
  children,
}: Readonly<{
  children: ReactNode;
}>) {
  const router = useRouter();
  const queryClientRef = useRef<QueryClient | null>(null);

  if (!queryClientRef.current) {
    queryClientRef.current = new QueryClient({
      queryCache: new QueryCache({
        onError: (error) => {
          void handleApiError(error);
        },
      }),
      mutationCache: new MutationCache({
        onError: (error) => {
          void handleApiError(error);
        },
      }),
    });
  }

  const queryClient = queryClientRef.current;

  const handleApiError = async (error: Error) => {
    if (!(error instanceof ApiError)) {
      return;
    }

    const currentQueryClient = queryClientRef.current;
    if (currentQueryClient && (error.status === 401 || error.status === 503)) {
      await currentQueryClient.cancelQueries({ predicate: () => true });
      await currentQueryClient.resetQueries({ predicate: () => true });
    }

    if (error.status === 401) {
      error.message = "Sesja wygasła. Zaloguj się ponownie.";
      void handleLogoutRedirect({ router, redirectPath: "/" });
    }

    if (
      error.status === 404 &&
      error.message.startsWith("No static resource")
    ) {
      error.message = "Wystąpił nieoczekiwany błąd";
    }

    if (error.status === 503) {
      router.push("/");
    }

    const message =
      error.message.trim().length > 0
        ? error.message
        : "Wystąpił nieoczekiwany błąd. Spróbuj ponownie.";

    toast.error(message, {
      id: "api-error-toast",
    });
  };

  return (
    <html lang="pl" className="overflow-hidden" suppressHydrationWarning>
      <head>
        <meta name="theme-color" content="#262626" />
        <link rel="icon" type="image/x-icon" href="/favicon.png" />
      </head>
      <body
        className={`${leagueGothic.className} ${leagueGothic.variable} ${materialSymbols.variable} text-primary-dark dark:text-secondary-gray overflow-hidden`}
      >
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          storageKey="theme"
        >
          <TitleProvider routes={GENERAL_APPLICATION_ROUTES}>
            <QueryClientProvider client={queryClient}>
              <Toaster toastOptions={{ style: { fontSize: "1.5rem" } }} />
              <BackgroundWrapper className="hero-background-wrapper">
                {children}
              </BackgroundWrapper>
            </QueryClientProvider>
          </TitleProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
