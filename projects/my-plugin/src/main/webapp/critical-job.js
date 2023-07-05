function enableCriticalView(criticalJobRegularExpression) {
    document.addEventListener("DOMContentLoaded", function() {
        if (criticalJobRegularExpression === undefined ||
            criticalJobRegularExpression == "") {
            return
        }

        var url = window.location.pathname
        var split = url.split("/")
        var jobIndex = split.indexOf("job")
        if (jobIndex == -1 || split.size() <= jobIndex) {
            return
        }

        var jobName = split[jobIndex + 1]
        var re = new RegExp(criticalJobRegularExpression);
        if (re.test(jobName)) {
            document.getElementById("header").classList.add("operation-job")
        }
    })
}