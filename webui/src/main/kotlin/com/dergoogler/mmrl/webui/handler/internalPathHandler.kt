package com.dergoogler.mmrl.webui.handler

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.card.defaultCardColors
import com.dergoogler.mmrl.ui.component.button.defaultFilledTonalButtonColors
import com.dergoogler.mmrl.webui.PathHandler
import com.dergoogler.mmrl.webui.asScriptResponse
import com.dergoogler.mmrl.webui.asStyleResponse
import com.dergoogler.mmrl.webui.model.Insets
import com.dergoogler.mmrl.webui.notFoundResponse
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.util.toCssValue
import java.io.IOException

fun internalPathHandler(
    options: WebUIOptions,
    insets: Insets
): PathHandler {
    val colorScheme = options.colorScheme
    val filledTonalButtonColors = colorScheme.defaultFilledTonalButtonColors
    val cardColors = colorScheme.defaultCardColors

    val assetsHandler = assetsPathHandler(options)

    val appColors = buildString {
        appendLine(":root {")
        appendLine("\t/* App Base Colors */")
        appendLine("\t--primary: ${colorScheme.primary.toCssValue()};")
        appendLine("\t--onPrimary: ${colorScheme.onPrimary.toCssValue()};")
        appendLine("\t--primaryContainer: ${colorScheme.primaryContainer.toCssValue()};")
        appendLine("\t--onPrimaryContainer: ${colorScheme.onPrimaryContainer.toCssValue()};")
        appendLine("\t--inversePrimary: ${colorScheme.inversePrimary.toCssValue()};")
        appendLine("\t--secondary: ${colorScheme.secondary.toCssValue()};")
        appendLine("\t--onSecondary: ${colorScheme.onSecondary.toCssValue()};")
        appendLine("\t--secondaryContainer: ${colorScheme.secondaryContainer.toCssValue()};")
        appendLine("\t--onSecondaryContainer: ${colorScheme.onSecondaryContainer.toCssValue()};")
        appendLine("\t--tertiary: ${colorScheme.tertiary.toCssValue()};")
        appendLine("\t--onTertiary: ${colorScheme.onTertiary.toCssValue()};")
        appendLine("\t--tertiaryContainer: ${colorScheme.tertiaryContainer.toCssValue()};")
        appendLine("\t--onTertiaryContainer: ${colorScheme.onTertiaryContainer.toCssValue()};")
        appendLine("\t--background: ${colorScheme.background.toCssValue()};")
        appendLine("\t--onBackground: ${colorScheme.onBackground.toCssValue()};")
        appendLine("\t--surface: ${colorScheme.surface.toCssValue()};")
        appendLine("\t--tonalSurface: ${colorScheme.surfaceColorAtElevation(1.dp).toCssValue()};")
        appendLine("\t--onSurface: ${colorScheme.onSurface.toCssValue()};")
        appendLine("\t--surfaceVariant: ${colorScheme.surfaceVariant.toCssValue()};")
        appendLine("\t--onSurfaceVariant: ${colorScheme.onSurfaceVariant.toCssValue()};")
        appendLine("\t--surfaceTint: ${colorScheme.surfaceTint.toCssValue()};")
        appendLine("\t--inverseSurface: ${colorScheme.inverseSurface.toCssValue()};")
        appendLine("\t--inverseOnSurface: ${colorScheme.inverseOnSurface.toCssValue()};")
        appendLine("\t--error: ${colorScheme.error.toCssValue()};")
        appendLine("\t--onError: ${colorScheme.onError.toCssValue()};")
        appendLine("\t--errorContainer: ${colorScheme.errorContainer.toCssValue()};")
        appendLine("\t--onErrorContainer: ${colorScheme.onErrorContainer.toCssValue()};")
        appendLine("\t--outline: ${colorScheme.outline.toCssValue()};\n")
        appendLine("\t--outlineVariant: ${colorScheme.outlineVariant.toCssValue()};")
        appendLine("\t--scrim: ${colorScheme.scrim.toCssValue()};\n")
        appendLine("\t--surfaceBright: ${colorScheme.surfaceBright.toCssValue()};")
        appendLine("\t--surfaceDim: ${colorScheme.surfaceDim.toCssValue()};")
        appendLine("\t--surfaceContainer: ${colorScheme.surfaceContainer.toCssValue()};")
        appendLine("\t--surfaceContainerHigh: ${colorScheme.surfaceContainerHigh.toCssValue()};")
        appendLine("\t--surfaceContainerHighest: ${colorScheme.surfaceContainerHighest.toCssValue()};")
        appendLine("\t--surfaceContainerLow: ${colorScheme.surfaceContainerLow.toCssValue()};")
        appendLine("\t--surfaceContainerLowest: ${colorScheme.surfaceContainerLowest.toCssValue()};")
        appendLine("\t/* Filled Tonal Button Colors */")
        appendLine("\t--filledTonalButtonContentColor: ${filledTonalButtonColors.contentColor.toCssValue()};")
        appendLine("\t--filledTonalButtonContainerColor: ${filledTonalButtonColors.containerColor.toCssValue()};")
        appendLine("\t--filledTonalButtonDisabledContentColor: ${filledTonalButtonColors.disabledContentColor.toCssValue()};")
        appendLine("\t--filledTonalButtonDisabledContainerColor: ${filledTonalButtonColors.disabledContainerColor.toCssValue()};")
        appendLine("\t/* Filled Card Colors */")
        appendLine("\t--filledCardContentColor: ${cardColors.contentColor.toCssValue()};")
        appendLine("\t--filledCardContainerColor: ${cardColors.containerColor.toCssValue()};")
        appendLine("\t--filledCardDisabledContentColor: ${cardColors.disabledContentColor.toCssValue()};")
        appendLine("\t--filledCardDisabledContainerColor: ${cardColors.disabledContainerColor.toCssValue()};")
        append("}")
    }

    return handler@{ path ->
        try {
            if (path.matches(Regex("^assets(/.*)?$"))) {
                return@handler assetsHandler(path.removePrefix("assets/"))
            }

            val inputStream = options.modId.sanitizedIdWithFileInputStream

            if (path.matches(Regex("scripts/require\\.js"))) {
                return@handler """(function() {
    // Configuration
    var BASE_MODULE_PATH = '/data/adb/modules';
    var CURRENT_MODULE_ID = '${options.modId.id}';
    
    // Module cache
    var moduleCache = {};
    
    // Get current working directory
    function getCwd() {
        return BASE_MODULE_PATH + '/' + CURRENT_MODULE_ID + '/webroot/';
    }
    
    // Module constructor
    function Module(id) {
        this.id = id;
        this.exports = {};
    }
    
    // Convert byte array to string
    function bytesToString(byteArray) {
        return String.fromCharCode.apply(null, byteArray);
    }
    
    // Resolve a path relative to cwd
    function resolvePath(relativePath) {
        if (relativePath.charAt(0) === '/') {
            return relativePath; // Absolute path
        }
        return getCwd() + relativePath;
    }
    
    // Resolve module path
    function resolveModulePath(moduleId) {
        // Handle absolute paths
        if (moduleId.charAt(0) === '/') {
            return [moduleId, moduleId + '.js'];
        }
        
        // Handle relative paths
        if (moduleId.indexOf('./') === 0 || moduleId.indexOf('../') === 0) {
            var basePath = resolvePath(moduleId);
            return [
                basePath,
                basePath + '.js',
                basePath + '/index.js'
            ];
        }
        
        // Handle module paths
        var parts = moduleId.split('/');
        var moduleBase, subPath;
        
        if (moduleId.charAt(0) === '@') {
            moduleBase = parts.slice(0, 2).join('/');
            subPath = parts.slice(2).join('/');
        } else {
            moduleBase = parts[0];
            subPath = parts.slice(1).join('/');
        }
        
        // Generate paths to try
        var pathsToTry = [];
        var baseModulePath = BASE_MODULE_PATH + '/' + moduleBase + '/webroot/';
        
        if (subPath) {
            pathsToTry.push(baseModulePath + subPath);
            pathsToTry.push(baseModulePath + subPath + '.js');
            pathsToTry.push(baseModulePath + subPath + '/index.js');
        } else {
            pathsToTry.push(baseModulePath + 'index.js');
        }
        
        return pathsToTry;
    }
    
    // File system loader using InputStream
    function loadFromFileSystem(filePath) {
        try {
            var input = $inputStream.open(filePath);
            var result = [];
            
            while (true) {
                var byte = input.read();
                if (byte === -1) break;
                result.push(byte);
            }
            
            input.close();
            return result;
        } catch (error) {
            throw new Error('File read error for ' + filePath + ': ' + error.message);
        }
    }
    
    // Load text file (for HTML includes)
    function loadTextFile(filePath) {
        try {
            var bytes = loadFromFileSystem(filePath);
            return bytesToString(bytes);
        } catch (e) {
            console.error('Failed to load file: ' + filePath, e);
            return 'Error loading ' + filePath;
        }
    }
    
    // HTML includes functionality
    function resolveIncludePath(relativePath) {
        return resolvePath(relativePath);
    }
    
    function loadHtmlIncludes() {
        var elements = document.querySelectorAll('[data-include]');
        for (var i = 0; i < elements.length; i++) {
            var el = elements[i];
            var relativePath = el.getAttribute('data-include');
            var filePath = resolveIncludePath(relativePath);
            var html = loadTextFile(filePath);
            el.innerHTML = html;
        }
    }
    
    // Main require function
    window.require = function(moduleId) {
        // Check cache first
        if (moduleCache[moduleId]) {
            return moduleCache[moduleId].exports;
        }
        
        // Create new module and cache it
        var module = new Module(moduleId);
        moduleCache[moduleId] = module;
        
        // Get possible paths to try
        var pathsToTry = resolveModulePath(moduleId);
        var lastError = null;
        
        for (var i = 0; i < pathsToTry.length; i++) {
            var filePath = pathsToTry[i];
            try {
                // Load module content
                var byteArray = loadFromFileSystem(filePath);
                var moduleCode = bytesToString(byteArray);
                
                // Create wrapper function
                var wrapperFn = new Function(
                    'module', 'exports', 'require', 
                    moduleCode + '\n//# sourceURL=' + filePath
                );
                
                // Execute the module
                wrapperFn.call(
                    module.exports, 
                    module, 
                    module.exports, 
                    require
                );
                
                return module.exports;
            } catch (error) {
                lastError = error;
                continue;
            }
        }
        
        // Clean up cache on error
        delete moduleCache[moduleId];
        throw new Error('Failed to load module "' + moduleId + '". Tried paths:\n' + pathsToTry.join('\n') + '\nLast error: ' + (lastError ? lastError.message : 'unknown'));
    };
    
    // Support for extension-less requires
    var originalRequire = window.require;
    window.require = function(moduleId) {
        try {
            return originalRequire(moduleId);
        } catch (e) {
            if (moduleId.slice(-3) !== '.js') {
                try {
                    return originalRequire(moduleId + '.js');
                } catch (e2) {
                    throw e;
                }
            }
            throw e;
        }
    };
    
    // Initialize HTML includes when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadHtmlIncludes);
    } else {
        loadHtmlIncludes();
    }
})();
""".trimIndent().asScriptResponse()
            }

            if (path.matches(Regex("insets\\.css"))) {
                return@handler insets.cssResponse
            }

            if (path.matches(Regex("colors\\.css"))) {
                return@handler appColors.asStyleResponse()
            }

            return@handler notFoundResponse
        } catch (e: IOException) {
            Log.e("mmrlPathHandler", "Error opening mmrl asset path: $path", e)
            return@handler notFoundResponse
        }
    }
}