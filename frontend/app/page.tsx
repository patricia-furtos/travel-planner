"use client"

import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import type { User } from "@supabase/supabase-js"
import { useRouter } from "next/navigation"
import { createTrip, getTrips, joinTrip, type Trip } from "@/lib/backend-api"
import { createClient } from "@/lib/supabase"

function formatTripDates(trip: Trip) {
  if (!trip.startDate && !trip.endDate) return "Datele nu au fost stabilite"
  const formatDate = (date: string) =>
    new Date(`${date}T00:00:00`).toLocaleDateString("ro-RO")

  if (trip.startDate && trip.endDate) {
    return `${formatDate(trip.startDate)} – ${formatDate(trip.endDate)}`
  }
  if (trip.startDate) return `Începe la ${formatDate(trip.startDate)}`
  if (trip.endDate) return `Se încheie la ${formatDate(trip.endDate)}`
  return "Datele nu au fost stabilite"
}

export default function DashboardPage() {
  const [user, setUser] = useState<User | null>(null)
  const [trips, setTrips] = useState<Trip[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [activeForm, setActiveForm] = useState<"create" | "join" | null>(null)
  const [name, setName] = useState("")
  const [destination, setDestination] = useState("")
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")
  const [inviteCode, setInviteCode] = useState("")
  const router = useRouter()

  useEffect(() => {
    let cancelled = false

    const loadDashboard = async () => {
      try {
        const supabase = createClient()
        const {
          data: { user: authenticatedUser },
          error: userError,
        } = await supabase.auth.getUser()
        if (userError) throw userError
        if (!authenticatedUser) {
          router.replace("/login")
          return
        }

        setUser(authenticatedUser)
        const userTrips = await getTrips()
        if (cancelled) return
        setTrips(userTrips)
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Nu s-au putut încărca excursiile.")
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    void loadDashboard()
    return () => {
      cancelled = true
    }
  }, [router])

  const handleLogout = async () => {
    const supabase = createClient()
    const { error: signOutError } = await supabase.auth.signOut()
    if (signOutError) {
      setError(signOutError.message)
      return
    }
    router.push("/login")
  }

  const handleCreateTrip = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setSuccess(null)
    setSaving(true)

    try {
      const trip = await createTrip({
        name,
        destination,
        startDate: startDate || null,
        endDate: endDate || null,
      })
      setTrips((currentTrips) => [trip, ...currentTrips])
      setName("")
      setDestination("")
      setStartDate("")
      setEndDate("")
      setActiveForm(null)
      setSuccess(`Excursia a fost creată. Codul de invitație: ${trip.inviteCode}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Excursia nu a putut fi creată.")
    } finally {
      setSaving(false)
    }
  }

  const handleJoinTrip = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setSuccess(null)
    setSaving(true)

    try {
      const trip = await joinTrip(inviteCode)
      setTrips((currentTrips) => [
        trip,
        ...currentTrips.filter((currentTrip) => currentTrip.id !== trip.id),
      ])
      setInviteCode("")
      setActiveForm(null)
      setSuccess(`Te-ai alăturat excursiei „${trip.name}”.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nu te-ai putut alătura excursiei.")
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <p className="text-slate-500">Se încarcă...</p>
      </div>
    )
  }

  if (!user) return null

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between">
        <h1 className="text-xl font-bold text-slate-900">TravelPlanner</h1>
        <div className="flex items-center gap-4">
          <span className="text-sm text-slate-600">
            Salut, <strong>{user.user_metadata?.full_name || user.email}</strong>
          </span>
          <button
            onClick={handleLogout}
            className="text-sm px-3 py-1.5 border border-slate-300 rounded-lg hover:bg-slate-100 transition"
          >
            Ieși din cont
          </button>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-10">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Excursiile mele</h2>
            <p className="text-sm text-slate-500">
              Planifică o călătorie sau alătură-te uneia cu un cod de invitație.
            </p>
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => {
                setError(null)
                setSuccess(null)
                setActiveForm(activeForm === "create" ? null : "create")
              }}
              className="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition"
            >
              + Excursie nouă
            </button>
            <button
              type="button"
              onClick={() => {
                setError(null)
                setSuccess(null)
                setActiveForm(activeForm === "join" ? null : "join")
              }}
              className="px-4 py-2 bg-white border border-slate-300 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-100 transition"
            >
              Alătură-te
            </button>
          </div>
        </div>

        {activeForm === "create" && (
          <form
            onSubmit={handleCreateTrip}
            className="mb-6 grid gap-4 rounded-2xl border border-slate-200 bg-white p-6 sm:grid-cols-2"
          >
            <h3 className="text-lg font-semibold text-slate-900 sm:col-span-2">
              Creează o excursie
            </h3>
            <label className="text-sm font-medium text-slate-700">
              Numele excursiei
              <input
                required
                maxLength={100}
                value={name}
                onChange={(event) => setName(event.target.value)}
                placeholder="ex: Weekend la Brașov"
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal"
              />
            </label>
            <label className="text-sm font-medium text-slate-700">
              Destinație
              <input
                maxLength={120}
                value={destination}
                onChange={(event) => setDestination(event.target.value)}
                placeholder="ex: Brașov, România"
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal"
              />
            </label>
            <label className="text-sm font-medium text-slate-700">
              Data de început
              <input
                type="date"
                value={startDate}
                onChange={(event) => setStartDate(event.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal"
              />
            </label>
            <label className="text-sm font-medium text-slate-700">
              Data de sfârșit
              <input
                type="date"
                value={endDate}
                onChange={(event) => setEndDate(event.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal"
              />
            </label>
            <div className="flex justify-end gap-2 sm:col-span-2">
              <button
                type="button"
                onClick={() => setActiveForm(null)}
                className="rounded-lg px-4 py-2 text-sm text-slate-600 hover:bg-slate-100"
              >
                Anulează
              </button>
              <button
                type="submit"
                disabled={saving}
                className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
              >
                {saving ? "Se creează..." : "Creează excursia"}
              </button>
            </div>
          </form>
        )}

        {activeForm === "join" && (
          <form
            onSubmit={handleJoinTrip}
            className="mb-6 flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-6 sm:flex-row sm:items-end"
          >
            <label className="flex-1 text-sm font-medium text-slate-700">
              Cod de invitație
              <input
                required
                maxLength={12}
                value={inviteCode}
                onChange={(event) => setInviteCode(event.target.value.toUpperCase())}
                placeholder="ex: ABCD2345EF"
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-normal uppercase"
              />
            </label>
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
            >
              {saving ? "Se alătură..." : "Alătură-te excursiei"}
            </button>
          </form>
        )}

        {error && (
          <p role="alert" className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            {error}
          </p>
        )}
        {success && (
          <p role="status" className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">
            {success}
          </p>
        )}

        {trips.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-12 text-center">
            <p className="text-slate-500">Nu ai nicio excursie planificată încă.</p>
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2">
            {trips.map((trip) => (
              <article
                key={trip.id}
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="text-lg font-semibold text-slate-900">{trip.name}</h3>
                    <p className="mt-1 text-sm text-slate-600">
                      {trip.destination || "Destinație nespecificată"}
                    </p>
                  </div>
                  <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600">
                    {trip.memberCount} {trip.memberCount === 1 ? "membru" : "membri"}
                  </span>
                </div>
                <p className="mt-4 text-sm text-slate-500">{formatTripDates(trip)}</p>
                <div className="mt-5 border-t border-slate-100 pt-4">
                  <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                    Cod de invitație
                  </p>
                  <p className="mt-1 font-mono text-lg font-semibold tracking-wider text-slate-900">
                    {trip.inviteCode}
                  </p>
                </div>
              </article>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
