package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val calcTabTourSteps: Map<TabTour, List<TabTourStep>> = emptyMap()

internal val calcScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "calc_zakat" to listOf(
        TabTourStep("", "Zakat calculator", "Work out your zakat in five steps: rates, gold and silver, cash, debts, then nisab. This screen has no audio."),
        TabTourStep("calc_zakat_due", "Zakat due", "Your zakat amount appears here and updates as you type."),
        TabTourStep("calc_zakat_status", "Status", "Tells you if zakat is due, not due, or if a rate is still missing.", optional = true),
        TabTourStep("calc_zakat_nisab_meter", "Nisab meter", "Shows how close your net wealth is to the nisab.", optional = true),
        TabTourStep("calc_zakat_share", "Share result", "Tap to send a summary of your totals and zakat due to any app."),
        TabTourStep("calc_zakat_clear", "Clear figures", "Tap to remove all amounts and weights. You confirm first; your currency and rates stay."),
        TabTourStep("calc_zakat_currency", "Pick currency", "Tap to choose the currency for all amounts and live rates."),
        TabTourStep("calc_zakat_unit", "Tola or gram", "Choose the weight unit. Weights and rates convert by themselves."),
        TabTourStep("calc_zakat_gold_rate", "Gold rate", "Price of pure 24K gold per unit. Type your local rate to use your own."),
        TabTourStep("calc_zakat_silver_rate", "Silver rate", "Price of silver per unit. It is needed for the nisab check, even if you own no silver."),
        TabTourStep("calc_zakat_live_status", "Live rate status", "Shows the date of the live rates, or a message when they could not load.", optional = true),
        TabTourStep("calc_zakat_refresh", "Refresh rates", "Tap to get today's live gold and silver rates again.", optional = true),
        TabTourStep("calc_zakat_use_live", "Use live rates", "You typed your own rates. Tap to switch back to live market rates.", optional = true),
        TabTourStep("calc_zakat_karat", "Gold purity", "Pick the karat of your gold. Most jewellery is 22K or 21K."),
        TabTourStep("calc_zakat_gold_weight", "Gold you own", "Enter the total weight of your gold. Its value shows below the field."),
        TabTourStep("calc_zakat_silver_weight", "Silver you own", "Enter the total weight of your silver. Its value shows below the field."),
        TabTourStep("calc_zakat_cash", "Cash and bank", "Enter cash in hand and money in your bank accounts."),
        TabTourStep("calc_zakat_business", "Business stock", "Enter the current value of goods you hold for trade."),
        TabTourStep("calc_zakat_receivables", "Money owed to you", "Enter loans you gave that you expect to get back."),
        TabTourStep("calc_zakat_more_assets", "More assets", "Tap to show or hide fields for investments, shares and other assets."),
        TabTourStep("calc_zakat_investments", "Investments and shares", "Enter the zakatable value of your investments and shares.", optional = true),
        TabTourStep("calc_zakat_other_assets", "Other assets", "Enter any other zakatable assets here.", optional = true),
        TabTourStep("calc_zakat_debts", "Debts due now", "Enter debts you must pay now. They are taken away from your wealth."),
        TabTourStep("calc_zakat_more_liabilities", "More liabilities", "Tap to show or hide fields for bills, taxes and instalments."),
        TabTourStep("calc_zakat_bills", "Bills and taxes", "Enter bills and taxes that are already due.", optional = true),
        TabTourStep("calc_zakat_instalments", "Upcoming instalments", "Enter only near-term principal payments, not the full loan or interest.", optional = true),
        TabTourStep("calc_zakat_nisab_mode", "Nisab basis", "Choose Auto, Silver or Gold. The note below explains which nisab is used."),
        TabTourStep("calc_zakat_lunar_year", "Lunar year switch", "Turn on when a full Hijri year has passed on your nisab-level wealth.", optional = true),
        TabTourStep("calc_zakat_set_date", "Set zakat date", "Tap to pick the date you first owned the nisab. The app then tracks your lunar year.", optional = true),
        TabTourStep("calc_zakat_hawl_tracker", "Lunar year tracker", "Shows your zakat date and how many days are left in your lunar year.", optional = true),
        TabTourStep("calc_zakat_change_date", "Change date", "Tap to pick a different zakat date.", optional = true),
        TabTourStep("calc_zakat_remove_date", "Remove date", "Tap to stop tracking the lunar year and use the switch instead.", optional = true),
        TabTourStep("calc_zakat_hadith", "Hadith sources", "Hadith cards with their references explain the zakat rate and nisab."),
        TabTourStep("calc_zakat_privacy", "Saved on this phone", "Your figures stay only on this phone. For personal cases, ask a trusted mufti.")
    ),
    "calc_mirath" to listOf(
        TabTourStep("", "Inheritance calculator", "Enter the estate and family to see each heir's Hanafi share. This screen has no audio."),
        TabTourStep("calc_mirath_estate", "Total estate", "Enter everything the deceased owned. This field is required."),
        TabTourStep("calc_mirath_funeral", "Funeral expenses", "Enter funeral costs. They are paid first, before any share."),
        TabTourStep("calc_mirath_debts", "Outstanding debts", "Enter debts of the deceased. They are paid before the estate is shared."),
        TabTourStep("calc_mirath_bequest", "Wasiyyat (bequest)", "Enter a bequest to non-heirs. Up to one third after expenses and debts is supported."),
        TabTourStep("calc_mirath_available", "Distributable estate", "Shows the bequest limit and what is left to share among heirs.", optional = true),
        TabTourStep("calc_mirath_gender", "Deceased male or female", "Pick whether the deceased was male or female. This changes the spouse options."),
        TabTourStep("calc_mirath_spouse", "Wives or husband", "Use minus and plus to set how many spouses survive."),
        TabTourStep("calc_mirath_children", "Sons and daughters", "Use minus and plus to set how many sons and daughters survive."),
        TabTourStep("calc_mirath_parents", "Father and mother", "Tick if the father or mother is alive."),
        TabTourStep("calc_mirath_siblings", "Brothers and sisters", "Set how many brothers and sisters survive. They can change the mother's share."),
        TabTourStep("calc_mirath_others", "Other heirs", "Tick for grandchildren, other relatives or special cases. These need a scholar's review."),
        TabTourStep("calc_mirath_confirm", "Confirm heirs", "Tick after checking that all surviving heirs are entered."),
        TabTourStep("calc_mirath_calculate", "Calculate shares", "Tap to work out each heir's share. The result appears below."),
        TabTourStep("calc_mirath_reset", "Reset", "Tap to clear all amounts and heirs and start again."),
        TabTourStep("calc_mirath_result", "Inheritance breakdown", "Shows the distributable estate and each deduction.", optional = true),
        TabTourStep("calc_mirath_share_row", "Each heir's share", "Each card shows the fraction, percent and amount, plus the rule behind it.", optional = true),
        TabTourStep("calc_mirath_share", "Share breakdown", "Tap to send the full breakdown to any app.", optional = true)
    )
)
