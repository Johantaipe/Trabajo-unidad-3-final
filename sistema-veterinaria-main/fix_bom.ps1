# Script para eliminar BOM de archivos Java
$javaFiles = Get-ChildItem -Recurse -Filter "*.java" -Path "src\main\java" | Select-Object -ExpandProperty FullName

$count = 0
foreach ($file in $javaFiles) {
    $bytes = [System.IO.File]::ReadAllBytes($file)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $noBom = $bytes[3..($bytes.Length - 1)]
        [System.IO.File]::WriteAllBytes($file, $noBom)
        Write-Host "BOM removed: $(Split-Path $file -Leaf)" -ForegroundColor Yellow
        $count++
    }
}
Write-Host "BOM cleanup done. Files fixed: $count" -ForegroundColor Green
