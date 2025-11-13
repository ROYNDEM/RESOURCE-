package com.roy.ngong.navigation

object AppDestinations {
    const val SIGN_UP_ROUTE = "sign_up_route"
    const val PROFILE_ROUTE = "profile_screen_route"
    const val SPLASH_ROUTE = "splash_route"
    const val LOGIN_ROUTE = "login_route"
    const val HOME_SCREEN_ROUTE = "home"
    const val CALENDAR_ROUTE = "calendar"
    const val ADD_EDIT_EVENT_ROUTE = "add_edit_event/{dateString}"
    const val RESOURCE_ENTRY_ROUTE = "resource_entry"
    const val RESOURCE_EDIT_ROUTE = "resource_edit"
    const val ADMIN_DASHBOARD_ROUTE = "admin_dashboard" // For the main admin area after login
    const val ADMIN_MANAGE_CONTROLS_ROUTE = "admin_manage_controls"
    const val ADMIN_REPORTS_LIST_ROUTE = "admin_reports_list"
    const val ADMIN_REPORTS_DETAIL_ROUTE = "admin_reports_detail/{date}"
    const val ADMIN_ADD_ENTRY_ROUTE = "admin_add_entry"
    fun reportDetailRoute(date: String): String{
        return "admin_reports_detail/$date"
    }
}

