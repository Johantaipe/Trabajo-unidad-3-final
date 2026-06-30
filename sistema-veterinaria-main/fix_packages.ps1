# Fix: corregir packages duplicados en shared.servicios y otros problemas
$BASE = "src\main\java\com\vetexpert\sistema_veterinaria"
$BASE_PKG = "com.vetexpert.sistema_veterinaria"

Write-Host "Corrigiendo packages duplicados..." -ForegroundColor Yellow

# Fixes puntuales: shared.servicios.servicios -> shared.servicios
$fixes = @(
    @{ 
        file = "$BASE\shared\servicios\entity\Servicio.java"
        old  = "package $BASE_PKG.shared.servicios.servicios.entity;"
        new  = "package $BASE_PKG.shared.servicios.entity;"
    },
    @{ 
        file = "$BASE\shared\servicios\repository\ServicioRepository.java"
        old  = "package $BASE_PKG.shared.servicios.servicios.repository;"
        new  = "package $BASE_PKG.shared.servicios.repository;"
    },
    @{ 
        file = "$BASE\shared\servicios\service\ServicioService.java"
        old  = "package $BASE_PKG.shared.servicios.servicios.service;"
        new  = "package $BASE_PKG.shared.servicios.service;"
    },
    @{ 
        file = "$BASE\shared\servicios\service\impl\ServicioServiceImpl.java"
        old  = "package $BASE_PKG.shared.servicios.servicios.service.impl;"
        new  = "package $BASE_PKG.shared.servicios.service.impl;"
    }
)

foreach ($fix in $fixes) {
    if (Test-Path $fix.file) {
        $content = [System.IO.File]::ReadAllText($fix.file, [System.Text.Encoding]::UTF8)
        if ($content.Contains($fix.old)) {
            $content = $content.Replace($fix.old, $fix.new)
            [System.IO.File]::WriteAllText($fix.file, $content, [System.Text.Encoding]::UTF8)
            Write-Host "FIXED: $(Split-Path $fix.file -Leaf)" -ForegroundColor Green
        } else {
            Write-Host "OK (ya correcto): $(Split-Path $fix.file -Leaf)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "SKIP (no existe): $($fix.file)" -ForegroundColor Yellow
    }
}

# Tambien verificar si hay otros archivos con packages duplicados en todo el proyecto
Write-Host ""
Write-Host "Verificando otros packages duplicados..." -ForegroundColor Yellow
$allJava = Get-ChildItem -Recurse -Filter "*.java" -Path $BASE | Select-Object -ExpandProperty FullName
$duplicates = @()
foreach ($f in $allJava) {
    $content = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)
    # Detectar patron vacunacion.vacunas o servicios.servicios o publico.portal etc
    if ($content -match "package $([regex]::Escape($BASE_PKG))\.[a-z]+\.[a-z]+\.[a-z]+\.[a-z]+\.[a-z]+") {
        Write-Host "  Posible duplicado en: $(Split-Path $f -Leaf) - linea: $(($content | Select-String '^package').Line)" -ForegroundColor Red
        $duplicates += $f
    }
}
if ($duplicates.Count -eq 0) {
    Write-Host "  No se detectaron mas packages duplicados." -ForegroundColor Green
}

Write-Host ""
Write-Host "Fix completado." -ForegroundColor Green
