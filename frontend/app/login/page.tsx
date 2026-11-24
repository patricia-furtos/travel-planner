"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import type { User } from "@supabase/supabase-js"
import { backendUrl } from "@/lib/backend-api"
import { createClient } from "@/lib/supabase"

export default function LoginPage() {
  const [isSignUp, setIsSignUp] = useState(false)
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [fullName, setFullName] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const router = useRouter()

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setNotice(null)
    setLoading(true)

    try {
      const supabase = createClient()
      let authUser: User | null = null
      let accessToken: string | null = null

      if (isSignUp) {
        const { data, error: signUpError } = await supabase.auth.signUp({
          email,
          password,
          options: {
            data: { full_name: fullName },
          },
        })
        if (signUpError) throw signUpError
        authUser = data.user
        accessToken = data.session?.access_token ?? null
      } else {
        const { data, error: signInError } = await supabase.auth.signInWithPassword({
          email,
          password,
        })
        if (signInError) throw signInError
        authUser = data.user
        accessToken = data.session?.access_token ?? null
      }

      // Sincronizam userul cu backend-ul Java Spring Boot
      if (authUser) {
        if (!accessToken && isSignUp) {
          setNotice("Contul a fost creat. Confirmă adresa de e-mail, apoi autentifică-te.")
          return
        }
        if (!accessToken) {
          throw new Error("Sesiunea Supabase lipsește. Autentifică-te din nou.")
        }

        const res = await fetch(`${backendUrl}/api/users/sync`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
          body: JSON.stringify({
            fullName: authUser.user_metadata?.full_name || fullName || email.split("@")[0],
            avatarUrl: null,
          }),
        })

        if (!res.ok) {
          throw new Error("Eroare la sincronizarea cu serverul Java.")
        }

        router.push("/")
        router.refresh()
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "A apărut o eroare.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-slate-900">
            {isSignUp ? "Creează un cont" : "Bine ai revenit"}
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Planifică vacanțe și împarte bugetul cu prietenii
          </p>
        </div>

        <form onSubmit={handleAuth} className="space-y-4">
          {isSignUp && (
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Nume complet
              </label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="ex: Andrei Popescu"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              />
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Email
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="nume@exemplu.ro"
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">
              Parolă
            </label>
            <input
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minim 6 caractere"
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>

          {error && (
            <div className="p-3 text-sm text-red-600 bg-red-50 rounded-lg border border-red-200">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 px-4 bg-slate-900 hover:bg-slate-800 text-white font-medium rounded-lg transition disabled:opacity-50"
          >
            {loading
              ? "Se procesează..."
              : isSignUp
              ? "Înregistrează-te"
              : "Intră în cont"}
          </button>
        </form>

        <div className="mt-6 text-center text-sm text-slate-600">
          {isSignUp ? "Ai deja cont?" : "Nu ai cont încă?"}{" "}
          <button
            type="button"
            onClick={() => {
              setIsSignUp(!isSignUp)
              setError(null)
              setNotice(null)
            }}
            className="font-semibold text-slate-900 hover:underline"
          >
            {isSignUp ? "Loghează-te" : "Creează unul"}
          </button>
        </div>
        {notice && (
          <p role="status" className="mt-4 rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">
            {notice}
          </p>
        )}
      </div>
    </div>
  )
}