(function () {
    var bundle = null;

    function applyPlaceholders(text, replacements) {
        if (!text || !replacements) return text;
        for (var key in replacements) {
            if (!replacements.hasOwnProperty(key)) continue;
            var value = replacements[key];
            if (value === null || value === undefined) continue;
            var safeKey = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
            text = text.replace(new RegExp(safeKey, 'g'), String(value));
        }
        return text;
    }

    this.StopwatchLang = {
        init: function (context) {
            if (!context || typeof context.getTranslations !== 'function') return;
            bundle = context.getTranslations('stopwatch');
        },
        get: function (key) {
            if (!bundle || !key) return null;
            return bundle.get(key);
        },
        format: function (key, replacements) {
            var raw = this.get(key);
            return applyPlaceholders(raw, replacements);
        }
    };
})();
