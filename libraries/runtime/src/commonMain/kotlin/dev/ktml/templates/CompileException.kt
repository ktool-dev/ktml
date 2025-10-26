package dev.ktml.templates

import dev.ktml.Context
import dev.ktml.util.CompileException

suspend fun Context.writeCompileException() {
    val exception: CompileException = required("exception")
    raw(TEMPLATE_HTML, 0, 3103)
    write(exception.errors.size)
    raw(TEMPLATE_HTML, 3103, 1)
    write(if (exception.errors.size == 1) "error" else "errors")
    raw(TEMPLATE_HTML, 3104, 348)
    for ((filePath, fileErrors) in exception.errors.groupBy { it.filePath }) {
        raw(TEMPLATE_HTML, 3452, 179)
        write(filePath)
        raw(TEMPLATE_HTML, 3631, 52)
        write(fileErrors.size)
        raw(TEMPLATE_HTML, 3683, 1)
        write(if (fileErrors.size == 1) "error" else "errors")
        raw(TEMPLATE_HTML, 3684, 50)
        for (error in fileErrors) {
            raw(TEMPLATE_HTML, 3734, 64)
            write(error.message)
            raw(TEMPLATE_HTML, 3798, 21)
        }
        raw(TEMPLATE_HTML, 3819, 11)
    }
    raw(TEMPLATE_HTML, 3830, 22)
}

private const val TEMPLATE_HTML: String = """<!DOCTYPE html>
<html lang="en"><head>
    <title>KTML Compiler Error</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="https://iili.io/Kr8ZhSs.png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body {
            background-color: #2a3035;
            min-height: 100vh;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }

        .error-container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 1rem;
        }

        .error-header {
            background: #091017;
            border-radius: 12px;
            padding: 2rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 2rem;
        }

        .logo-container {
            flex-shrink: 0;
        }

        .error-card {
            background: white;
            border-radius: 12px;
            padding: 0;
            margin-bottom: 1rem;
            overflow: hidden;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .file-header {
            background: #f8f9fa;
            padding: 1rem 1.5rem;
            border-bottom: 2px solid #e9ecef;
        }

        .file-path {
            font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
            font-size: 1rem;
            font-weight: 600;
            color: #2d3748;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .error-item {
            padding: 1.5rem;
            border-bottom: 1px solid #f0f0f0;
        }

        .error-item:last-child {
            border-bottom: none;
        }

        .error-message {
            background: #fff5f5;
            border: 1px solid #fed7d7;
            border-radius: 8px;
            padding: 1rem;
            margin: 0;
            font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
            font-size: 0.875rem;
            color: #c53030;
            white-space: pre-wrap;
            word-wrap: break-word;
            line-height: 1.6;
        }

        .error-header .badge {
            font-size: 1rem;
            font-weight: 600;
            margin-left: 0.5rem;
            vertical-align: middle;
        }

        h1 {
            color: #FFFFFF;
            font-weight: 700;
            margin: 0;
        }

        .subtitle {
            color: #718096;
            margin-top: 0.5rem;
            font-size: 1rem;
        }
    </style>
</head><body>
<div class="error-container">
    <div class="error-header">
        <div>
            <h1>
                Compiler Error
                <span class="badge text-bg-danger"> </span>
            </h1>
            <p class="subtitle mb-0">
                <i class="bi bi-info-circle me-1"></i>
                Fix the errors below to continue development
            </p>
        </div>
        <div class="logo-container">
            <img src="https://iili.io/Kr86Zx9.png" alt="KTML Logo">
        </div>
    </div>

    <div class="error-card">
        <div class="file-header">
            <div class="file-path">
                <i class="bi bi-file-earmark-code text-danger"></i>
                
                <span class="badge text-bg-danger"> </span>
            </div>
        </div>
        <div class="error-item">
            <pre class="error-message"></pre>
        </div>
    </div>
</div>
</body></html>"""
