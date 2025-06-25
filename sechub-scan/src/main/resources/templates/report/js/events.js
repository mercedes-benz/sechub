// report.js

document.getElementById("startScan")?.addEventListener("click", () => {
    window.dispatchEvent(new CustomEvent("START_SCAN"));
});

document.getElementById("goToWebUi")?.addEventListener("click", () => {
    window.dispatchEvent(new CustomEvent("GO_TO_WEB_UI"));
});

document.querySelectorAll(".markFalsePositive").forEach((element) => {
    element.addEventListener("change", () => {
        const findingId = element.id;
        const type = element.checked ? "MARK_FALSE_POSITIVE" : "UNMARK_FALSE_POSITIVE";
        window.dispatchEvent(new CustomEvent(type, { detail: { findingId } }));
    });
});

document.querySelectorAll(".codeScanEntryLocation").forEach((element) => {
    element.addEventListener("click", (event) => {
        event.preventDefault();
        window.dispatchEvent(new CustomEvent("JUMP_TO_LOCATION", {
            detail: {
                location: element.dataset.location,
                line: element.dataset.line,
                column: element.dataset.column
            }
        }));
    });
});