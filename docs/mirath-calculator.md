# Wirasat (Mirath) calculator

Available in More → Daily tools. Offline Sunni Hanafi estimate for surviving spouses, parents and direct children. Amounts remain in screen state; no estate amounts or family details are sent to analytics.

Deduction order: reasonable funeral expenses, outstanding debts, then a valid non-heir bequest of at most one third of the remaining estate. Invalid amounts, excessive bequests, unconfirmed heir lists and special circumstances block calculation.

The engine uses exact reduced fractions for collective and per-person shares. It handles a father's residue, children in a 2:1 ratio, Umariyyatain, proportional Awl and Hanafi Radd excluding spouses. Siblings reduce the mother's fixed share when there are at least two, even if blocked by the father or son. Sibling inheritance without a father or son is outside the supported scope. Grandchildren, grandparents, other relatives, pregnancy, disputed eligibility and bequests to heirs require scholar review. A spouse-only or empty heir list leaves an explicitly unallocated remainder.

Displayed monetary values round independently to two decimal places; exact fractions are authoritative within the supported calculation. Users must reconcile rounding and have a qualified scholar verify the complete family and applicable rules before distribution.

References:
- Quran, An-Nisa 4:11–12: https://legacy.quran.com/4/11-12
- Parents and spouse / Umariyyatain: https://islamweb.net/en/fatwa/243838/division-of-inheritance-among-a-wife-a-father-and-a-mother
- Hanafi Radd excluding husband: https://islamqa.org/hanafi/seekersguidance-hanafi/31902/converts-and-inheritance/
- Hanafi inheritance tables, including Awl and Return: https://tile.loc.gov/storage-services/service/ll/llscd/23014769/23014769.pdf

Validation: `:app:testDebugUnitTest --tests com.example.ui.screens.MirathCalculatorTest`.

## Usability and validation improvements (2026-10-10)

- Amount fields show inline errors and explain required/optional inputs. Plain decimal input accepts local digit scripts and surrounding whitespace; commas, signs, scientific notation and more than two decimal places are rejected to avoid ambiguous or excessively scaled amounts.
- A live estate summary shows the net estate and the maximum bequest, rounded down to the nearest hundredth so it cannot exceed the exact one-third limit.
- Whole-row checkboxes and labelled increment/decrement controls support touch and screen readers. Selecting the already-active deceased gender preserves the spouse count.
- Calculate scrolls to the result. Any edited input invalidates the displayed result until Calculate is pressed again.
- Results show the deduction trail, exact group/per-person fractions, monetary amounts, percentages and proportional bars. Excluded siblings and any unallocated balance are explained.
- A signed rounding difference compares the estate with all rounded individual payouts and the unallocated balance; positive means left over, negative means overpaid. Exact fractions remain authoritative.
- Share breakdown opens Android's share chooser with the deduction trail, rules, adjustments, unallocated balance and review/rounding notes. No report is sent until the user selects a destination.
- Tests cover local digit input, exponent rejection, conservation across supported family combinations, per-person ratios and editing after a successful calculation.
