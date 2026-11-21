"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { createClient } from "@/lib/supabase"

export default function DashboardPage() {
  const [user, setUser] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()
  const supabase = createClient()

  useEffect(() => {
    const checkUser = async () => {
      const { data: { user } } = await supabase.auth.getUser()
      if (!user) {
        router.push("/login")
      } else {
        setUser(user)
      }
      setLoading(false)
    }
    checkUser()
  }, [router, supabase])

  const handleLogout = async () => {
    await supabase.auth.signOut()
    router.push("/login")
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
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Excursiile mele</h2>
            <p className="text-sm text-slate-500">
              Aici vor apărea vacanțele create de tine sau la care ai fost invitat.
            </p>
          </div>
          <button className="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition">
            + Excursie nouă
          </button>
        </div>

        <div className="bg-white border border-dashed border-slate-300 rounded-2xl p-12 text-center">
          <p className="text-slate-500">Nu ai nicio excursie planificată încă.</p>
        </div>
      </main>
    </div>
  )
}