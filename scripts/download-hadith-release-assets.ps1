$ErrorActionPreference = 'Stop'

$outputDir = Join-Path $PSScriptRoot '..\hadith-release-assets'
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$assets = [System.Collections.Generic.List[object]]::new()

$arabicSets = @(
    @{ Id='bukhari'; First=922; Count=2 },
    @{ Id='muslim'; First=973; Count=2 },
    @{ Id='abu-dawud'; First=896; Count=2 },
    @{ Id='tirmidhi'; First=914; Count=5 },
    @{ Id='nasai'; First=908; Count=6 },
    @{ Id='ibn-majah'; First=898; Count=2 },
    @{ Id='muwatta'; First=920; Count=2 },
    @{ Id='musnad-ahmad'; First=900; Count=6 },
    @{ Id='darimi'; First=906; Count=2 }
)

foreach ($set in $arabicSets) {
    for ($volume = 1; $volume -le $set.Count; $volume++) {
        $fileId = $set.First + $volume - 1
        $assets.Add([pscustomobject]@{
            Name = "ar-$($set.Id)-v$volume.pdf"
            Url = "https://data2.dawateislami.net/Data/Books/Download/ar/pdf/2014/$fileId-1.pdf"
        })
    }
}

# Only Dawat-e-Islami's own PDFs are mirrored; the Hindi (Ekummat) and English (Emaan Library/
# OpenMaktaba) editions were removed from the Hadith Library.

$failures = [System.Collections.Generic.List[string]]::new()
foreach ($asset in $assets) {
    $destination = Join-Path $outputDir $asset.Name
    if (Test-Path $destination) {
        $header = Get-Content -LiteralPath $destination -Encoding Byte -TotalCount 5
        if ([Text.Encoding]::ASCII.GetString($header) -eq '%PDF-') {
            Write-Host "Verified existing $($asset.Name)"
            continue
        }
        Remove-Item -LiteralPath $destination
    }

    Write-Host "Downloading $($asset.Name)"
    & curl.exe --fail --location --retry 4 --retry-delay 3 --connect-timeout 30 --output $destination $asset.Url
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path $destination)) {
        $failures.Add($asset.Name)
        continue
    }
    $header = Get-Content -LiteralPath $destination -Encoding Byte -TotalCount 5
    if ([Text.Encoding]::ASCII.GetString($header) -ne '%PDF-') {
        $failures.Add("$($asset.Name) (not a PDF)")
        Remove-Item -LiteralPath $destination
    }
}

# Only the listed assets are hashed, so stale PDFs left in the folder are never re-published.
$names = $assets | ForEach-Object { $_.Name }
$verified = Get-ChildItem -LiteralPath $outputDir -Filter '*.pdf' | Where-Object { $names -contains $_.Name } | Sort-Object Name
$manifest = foreach ($file in $verified) {
    $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $file.FullName).Hash.ToLowerInvariant()
    "$hash  $($file.Name)"
}
[IO.File]::WriteAllLines((Join-Path $outputDir 'SHA256SUMS.txt'), @($manifest), [Text.UTF8Encoding]::new($false))

Write-Host "Verified PDFs: $($verified.Count) / $($assets.Count)"
Write-Host "Total bytes: $(($verified | Measure-Object Length -Sum).Sum)"
if ($failures.Count -gt 0) {
    Write-Error "Failed assets: $($failures -join ', ')"
}
