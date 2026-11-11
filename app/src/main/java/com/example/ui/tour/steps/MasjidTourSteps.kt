package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val masjidTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.MASJID to listOf(
        TabTourStep("", "Masjid timings nearby", "Find masjids near you and see Azan and Jamaat times shared by people who pray there."),
        TabTourStep("masjid_add_header", "Add a masjid", "Tap here to add a masjid that is missing from the list.", optional = true),
        TabTourStep("masjid_refresh", "Refresh the list", "Tap to reload nearby masjids and their latest timings. You can also pull down to refresh."),
        TabTourStep("masjid_delete_data", "Delete my data", "Removes the masjids, timings and votes you shared. You will be asked to confirm first.", optional = true),
        TabTourStep("masjid_permission", "Allow location", "Location is needed to find masjids near you. Tap Settings if you denied it before.", optional = true),
        TabTourStep("masjid_maps", "Search in Google Maps", "Opens Google Maps to search for masjids around your current location.", optional = true),
        TabTourStep("masjid_card", "Masjid card", "Each card shows distance and the next Jamaat time. Tap a card to open its full details.", optional = true),
        TabTourStep("masjid_add", "Add a missing masjid", "Tap Add Masjid, enter its name and address, and it is shared with the community.", optional = true),
    )
)

internal val masjidScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "masjid_detail" to listOf(
        TabTourStep("", "Masjid details", "See this masjid's timings, help keep them correct, and get directions."),
        TabTourStep("masjid_detail_refresh", "Refresh details", "Tap to load the latest timings and votes for this masjid.", optional = true),
        TabTourStep("masjid_report_content", "Report content", "Use the flag to report a wrong name or wrong timings for this masjid.", optional = true),
        TabTourStep("masjid_directions", "Get directions", "Opens Google Maps with directions to this masjid."),
        TabTourStep("masjid_proximity", "Your distance", "Shows how far you are. You must be at the masjid to add timings or vote. Tap to update your location."),
        TabTourStep("masjid_timings", "Current timings", "Azan and Jamaat times for each prayer. The next Jamaat is highlighted. Vote Correct or Wrong below."),
        TabTourStep("masjid_update_timings", "Add or update timings", "Tap to enter the Azan and Jamaat time for each prayer. This works only when you are at the masjid."),
        TabTourStep("masjid_verify", "Confirm the masjid", "Tap Yes if this masjid is really here, or Report if it is not."),
        TabTourStep("masjid_history", "Update history", "Tap Show to see past timing changes and when they were made."),
    )
)
