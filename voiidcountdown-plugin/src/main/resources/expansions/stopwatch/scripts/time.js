(function () {
    function pad2(value) {
        return ('0' + value).slice(-2);
    }

    function pad3(value) {
        return ('00' + value).slice(-3);
    }

    function parseTimeToSeconds(time) {
        if (!time || typeof time !== 'string' || !/^\d{1,2}:\d{2}:\d{2}$/.test(time)) {
            return null;
        }
        var parts = time.split(':');
        var hours = parseInt(parts[0], 10);
        var minutes = parseInt(parts[1], 10);
        var seconds = parseInt(parts[2], 10);
        if (isNaN(hours) || isNaN(minutes) || isNaN(seconds)) {
            return null;
        }
        if (minutes > 59 || seconds > 59) {
            return null;
        }
        return (hours * 3600) + (minutes * 60) + seconds;
    }

    function splitDigits(value) {
        if (!value || value.length < 2) return ['0', '0'];
        return [String(value.charAt(0)), String(value.charAt(1))];
    }

    function formatParts(totalMillis) {
        var safeMillis = Math.max(0, totalMillis);
        var totalSeconds = Math.floor(safeMillis / 1000);
        var hours = Math.floor(totalSeconds / 3600);
        var minutes = Math.floor((totalSeconds % 3600) / 60);
        var seconds = totalSeconds % 60;
        var millis = safeMillis % 1000;

        var hh = pad2(hours);
        var mm = pad2(minutes);
        var ss = pad2(seconds);
        var ms = pad3(millis);

        return {
            hh: hh,
            mm: mm,
            ss: ss,
            ms: ms,
            hDigits: splitDigits(hh),
            mDigits: splitDigits(mm),
            sDigits: splitDigits(ss)
        };
    }

    function formatText(template, totalMillis) {
        var parts = formatParts(totalMillis);
        var result = String(template);
        result = result
            .replace(/%HH%/g, parts.hh)
            .replace(/%MM%/g, parts.mm)
            .replace(/%SS%/g, parts.ss)
            .replace(/%H1%/g, parts.hDigits[0])
            .replace(/%H2%/g, parts.hDigits[1])
            .replace(/%M1%/g, parts.mDigits[0])
            .replace(/%M2%/g, parts.mDigits[1])
            .replace(/%S1%/g, parts.sDigits[0])
            .replace(/%S2%/g, parts.sDigits[1])
            .replace(/%MS1%/g, parts.ms.charAt(0))
            .replace(/%MS2%/g, parts.ms.charAt(1))
            .replace(/%MS3%/g, parts.ms.charAt(2))
            .replace(/%MS%/g, parts.ms);
        return result;
    }

    this.StopwatchTime = {
        parseTimeToSeconds: parseTimeToSeconds,
        formatText: formatText
    };
})();
