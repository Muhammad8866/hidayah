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

$hindiSets = @(
    @{ Id='bukhari'; Sources=@('1mpn0z-DLLe1ZLdvXcdHZj9aSXTzWq4rd','13dUFaCavVt6orbwy8DA9gqwzjIpvGiq_','1_mwKYnn_zu7LDl6JYwmfInsYc8uB69bg','1sd0QqYqnsdtBKgUPsDXs4fjuGlLiMAeC','1XKp7lNXesYW6OdV8n5tO707Ov-eEgHdM','1-n4rEk4nxaif95wOxmvLun-4sBOc0jUs','1SvUlWGu0wpkbmMxBvUYAVKMxRxB1dYm8','1errBOG1KVFlHOi32KqaiyEMh-HcgZiPT') },
    @{ Id='muslim'; Sources=@('1jiHhihSJ2CxGdZEu0LbvnxBdidGjy-ha','15EMujAmV2pM1dJf_oIcSA50Jdhb1kY9C','1F_ZKtndSPjM5lvnfipMxj8xOTS4l_93D','1zk21gW4EPQh1v7BvgPHztZtvHFhlsZLd','1quFFndtvkhtocXWYnsQ6pnLvmVINZsVH','1SAFrPr2ArObc5r_bvdMyfpwi9c9zAPrJ','1jchF3VULXWCSMsIEhYIj-i6S2u8CNco8','1hnCSrxFU-NGr1bitkDscRJD3Xal_7IPw') },
    @{ Id='tirmidhi'; Sources=@('1PrY5MAp44r4TcjpqwfUWujOpZecVWKr8','1bSjGN-d1JE-pcW9DHipiYA5z0TxL181L','1_8IMFcZyu_S8eWdKTcXvqTTHc3TqHE9A','1fOmimXIHrKnrdgF0ubJtTGl8P1oQj2Oi') },
    @{ Id='abu-dawud'; Sources=@('129B1LVhY0UWWVU0DVYbCGoZ2kVqcOhsy','1ne9fTBvHGloup8lWcrDrNDeMQ_ACkh5Z','1VXC7ruBk-Ve58f5U6TwGuFjDuZev0Y_v','1uNGl5l7_dysdgfkiFOoeFGbDql6rxG9_','1ysLU8FgtqzOXHLPxXvk8ZSRkWoO3Zj10','1Lm6yRIFKjQy7OIRH_qqLmdO1x4xDMF-e') },
    @{ Id='nasai'; Sources=@('1Psa4gMUSU1R_we6sC5r1NLVQAOmpBa-c','1SHkf9JFYIgAT6LTvXNNBN6GuoN_nYN9W','1JtiqH_n0-0Tz-LyHDpsM4bRrpc28mgJD','1OjMnPTJ4xWyXH_gDsVKMWmYgkKlCEWp9','1_ZNBFjrnwRc-aTRuqimwEP0y8Jd7oicU','https://archive.org/download/sunan-nasai-hindi/sunan-nasai-hindi-vol-6.pdf','13VnK9ypKdeZEY-1t2rCC9b6Ydf97sZTP') },
    @{ Id='ibn-majah'; Sources=@('1_QeCsGAuU5mJl1kTtx4IPAJKBB0iD328') }
)

foreach ($set in $hindiSets) {
    for ($index = 0; $index -lt $set.Sources.Count; $index++) {
        $source = $set.Sources[$index]
        $url = if ($source.StartsWith('https://')) { $source } else { "https://drive.usercontent.google.com/download?id=$source&export=download&confirm=t" }
        $assets.Add([pscustomobject]@{ Name="hi-$($set.Id)-v$($index + 1).pdf"; Url=$url })
    }
}

$assets.Add([pscustomobject]@{ Name='en-bukhari-complete.pdf'; Url='https://www.emaanlibrary.com/wp-content/uploads/2019/10/Sahih_Al-Bukhari.pdf' })
$assets.Add([pscustomobject]@{ Name='en-muslim-complete.pdf'; Url='https://www.emaanlibrary.com/wp-content/uploads/2019/10/Sahih_Muslim.pdf' })
$assets.Add([pscustomobject]@{ Name='en-tirmidhi-complete.pdf'; Url='https://archive.org/download/islamic-english-books-collection/All%20in%20One-Jami%20At-Tirmidhi-Eng.pdf' })
$assets.Add([pscustomobject]@{ Name='en-abu-dawud-complete.pdf'; Url='https://archive.org/download/islamic-english-books-collection/All%20in%20One-Sunan%20Abu%20Dawud-Eng.pdf' })
$assets.Add([pscustomobject]@{ Name='en-nasai-v1-5.pdf'; Url='https://archive.org/download/islamic-english-books-collection/All%20in%20One-Sunan%20Nasai-Eng-Vol%201-5.pdf' })
$assets.Add([pscustomobject]@{ Name='en-nasai-v6.pdf'; Url='https://archive.org/download/islamic-english-books-collection/All%20in%20One-Sunan%20Nasai-Eng-Vol%206.pdf' })
$assets.Add([pscustomobject]@{ Name='en-ibn-majah-complete.pdf'; Url='https://archive.org/download/islamic-english-books-collection/All%20in%20One-Sunan-Ibn%20Majah-Eng.pdf' })

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

$verified = Get-ChildItem -LiteralPath $outputDir -Filter '*.pdf' | Sort-Object Name
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
