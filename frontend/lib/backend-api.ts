import { createClient } from "@/lib/supabase"

export const backendUrl = (
  process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080"
).replace(/\/+$/, "")

export type Trip = {
  id: string
  name: string
  destination: string | null
  startDate: string | null
  endDate: string | null
  inviteCode: string
  ownerId: string
  ownerName: string | null
  memberCount: number
  createdAt: string
}

export type CreateTripInput = {
  name: string
  destination: string
  startDate: string | null
  endDate: string | null
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null
}

async function backendRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const supabase = createClient()
  const {
    data: { session },
    error: sessionError,
  } = await supabase.auth.getSession()

  if (sessionError) throw sessionError
  if (!session?.access_token) {
    throw new Error("Sesiunea a expirat. Autentifică-te din nou.")
  }

  const headers = new Headers(options.headers)
  headers.set("Authorization", `Bearer ${session.access_token}`)
  if (options.body) headers.set("Content-Type", "application/json")

  const response = await fetch(
    `${backendUrl}${path}`,
    { ...options, headers },
  )
  const payload: unknown = await response.json()

  if (!response.ok) {
    const message =
      isRecord(payload) && typeof payload.detail === "string"
        ? payload.detail
        : isRecord(payload) && typeof payload.message === "string"
          ? payload.message
          : `Cererea către server a eșuat (${response.status}).`
    throw new Error(message)
  }

  return payload as T
}

export function getTrips() {
  return backendRequest<Trip[]>("/api/trips")
}

export function createTrip(input: CreateTripInput) {
  return backendRequest<Trip>("/api/trips", {
    method: "POST",
    body: JSON.stringify(input),
  })
}

export function joinTrip(inviteCode: string) {
  return backendRequest<Trip>("/api/trips/join", {
    method: "POST",
    body: JSON.stringify({ inviteCode }),
  })
}
