/* Copyright 2014 Mozilla Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

if (!pdfjsLib.getDocument || !pdfjsViewer.PDFPageView) {
    alert('Please build the pdfjs-dist library using\n' +
        '  `gulp dist-install`');
}

pdfjsLib.GlobalWorkerOptions.workerSrc =
    'pdfjs-dist/build/pdf.worker.js';

const CMAP_URL = 'pdfjs-dist/web/cmaps/';
const CMAP_PACKED = true;
const SCALE = 1.0;
const SCALE_FIXED = SCALE / 0.75;

let pageNumber = 1;
let numberOfPages;
let DEFAULT_URL;
let rectangles = [];

const container = $('#pageContainer')[0];
const prevPageButton = $("#pdf-prev");
const nextPageButton = $("#pdf-next");

prevPageButton.on('click', function showPreviousPage() {
    if (pageNumber !== 1) {
        pageNumber--;
        render();
        disableEnablePrevNextButtons();
    }
});

nextPageButton.on('click', function showNextPage() {
    if (pageNumber !== numberOfPages) {
        pageNumber++;
        render();
        disableEnablePrevNextButtons();
    }
});

function disableEnablePrevNextButtons() {
    if (pageNumber === 1) {
        prevPageButton.prop("disabled", true);
    } else {
        prevPageButton.prop("disabled", false);
    }
    if (pageNumber === numberOfPages) {
        nextPageButton.prop("disabled", true);
    } else {
        nextPageButton.prop("disabled", false);
    }
}

disableEnablePrevNextButtons();

function updateUuidForm() {
    $("#formUUID").val(sessionStorage.getItem("app-UUID"));
}

function getPdfFileUrl() {
    $.ajax({
        type: "get",
        url: '/api/files/' + sessionStorage.getItem("app-UUID"),
        success: function (data) {
            if (data !== "") {
                DEFAULT_URL = data;
                $("#file-link").attr("href", data).html(data);
                render();
            }
        }
    });
}

function getUUID() {
    $.ajax({
        type: "get",
        url: '/api/uuid',
        success: function (uuid) {
            if (uuid !== "") {
                if (sessionStorage.getItem("app-UUID") === null) {
                    console.log(uuid);
                    sessionStorage.setItem("app-UUID", uuid);
                }
            }
        }
    });
}

function getMessage() {
    $.ajax({
        type: "get",
        url: '/api/message/' + sessionStorage.getItem("app-UUID"),
        success: function (data) {
            if (data !== "") {
                $("#message").html(data);
            }
        }
    });
}

getUUID();
updateUuidForm();
getPdfFileUrl();
getMessage();

$("#do-refactor").on('click', function doAnonymization() {

    $("#do-refactor").prop("disabled", true);
    let filteredRects = rectangles.filter(function (e) {
        return e.marked === true
    });
    $.ajax({
        url: "/api/rectangles/" + sessionStorage.getItem("app-UUID"),
        method: 'POST',
        data: JSON.stringify(filteredRects),
        contentType: 'application/json',
        success: function () {
            getMessage();
        }
    })
});

$("#start-over").on('click', function startOver() {
    sessionStorage.removeItem("app-UUID");
    getUUID();
    updateUuidForm();
    location.reload();
});

$("#pdf-meta").hide();
$("#data-types").hide();

$(document).ready(
    function () {
        $('input:file').change(
            function () {
                if ($(this).val()) {
                    $('input:submit').attr('disabled', false);
                }
            }
        );
    }
);

const startAjaxGet = (function () {
    let executed = false;
    return function () {
        if (!executed) {
            executed = true;
            getRectangles();
        }
    };
})();

function render() {
    const loadingTask = pdfjsLib.getDocument({
        url: DEFAULT_URL,
        cMapUrl: CMAP_URL,
        cMapPacked: CMAP_PACKED
    });
    $(".page").remove();
    loadingTask.promise.then(function (pdfDocument) {
        numberOfPages = pdfDocument.numPages;
        document.getElementById('page_count').textContent = numberOfPages;
        document.getElementById('page_num').textContent = pageNumber;
        return pdfDocument.getPage(pageNumber).then(function (pdfPage) {
            const pdfPageView = new pdfjsViewer.PDFPageView({
                container: container,
                id: pageNumber,
                scale: SCALE,
                defaultViewport: pdfPage.getViewport({scale: SCALE}),
                // annotationLayerFactory: new pdfjsViewer.DefaultAnnotationLayerFactory(),
                textLayerFactory: new pdfjsViewer.DefaultTextLayerFactory()
            });
            pdfPageView.setPdfPage(pdfPage);
            $("#upload-form").hide();
            $("#welcome-screen").hide();
            $("#pdf-meta").show();
            $("#data-types").show();
            $("#files-list").show();
            if (pageNumber === numberOfPages) {
                nextPageButton.prop("disabled", true);
            } else {
                nextPageButton.prop("disabled", false);
            }

            for (let i in rectangles) {
                if (rectangles.hasOwnProperty(i)) {
                    rectangles[i].drew = false;
                }
            }

            startAjaxGet();

            return pdfPageView.draw();
        })
    });
}

function hasId(prop, value, data) {
    return data.some(function (obj) {
        return prop in obj && obj[prop] === value;
    });
}

function getRectangles() {
    $.ajax({
        type: "get",
        url: "/api/rectangles/" + sessionStorage.getItem("app-UUID"),
        contentType: 'application/json',
        success: function (data) {
            if (!$.trim(data)) {
                console.log("No new data, trying again in 1 second.");
                setTimeout(function () {
                    getRectangles();
                }, 1000)
            } else {
                for (const o in data) {
                    if (data.hasOwnProperty(o)) {
                        if (!hasId('id', data[o].id, rectangles)) {

                            rectangles.push(data[o]);
                            // console.log("Object pushed: " + JSON.stringify(data[o]));
                        }
                    }
                }
                // var additionalRectListJS = JSON.stringify(data);
                // console.log("Received following rectangle list: " + additionalRectListJS);
            }
        },
        error: function () {
            console.log("Fail, trying again in 1 second.");
            setTimeout(function () {
                getRectangles();
            }, 1000)
        }
    });
}

$("#getMoreBoxes").on('click', function () {
    getRectangles();
});

function drawRedRec(x, y, w, h, rectNum) {
    const ctx = getContext(rectNum);
    ctx.strokeStyle = 'rgba(255,0,0,1)';
    ctx.beginPath();
    ctx.rect(SCALE_FIXED * x, SCALE_FIXED * y, SCALE_FIXED * w, SCALE_FIXED * h);
    ctx.stroke();
    rectangles[rectNum].drew = true;
    // console.log("Red rectangle has been drawn: " + rectNum);
}

function drawBlackRec(x, y, w, h, rectNum) {
    const ctx = getContext(rectNum);
    ctx.strokeStyle = 'rgba(0,0,0,1)';
    ctx.beginPath();
    ctx.rect(SCALE_FIXED * x, SCALE_FIXED * y, SCALE_FIXED * w, SCALE_FIXED * h);
    ctx.fillStyle = "black";
    ctx.fill();
}

function getContext(rectNum) {
    const $cloned = $("#pageContainer > div > div.canvasWrapper > canvas[id^=page]:last").first().clone();
    $cloned.clone().prop('id', 'rect' + rectNum).prop('style', 'position: absolute')
        .removeAttr("moz-opaque").prependTo("#pageContainer > div > div.canvasWrapper");
    const c = $("canvas[id^=rect]");
    return c.get(0).getContext("2d");
}

function removeRectangle(rectNum) {
    $("#pageContainer > div > div.canvasWrapper > canvas[id=rect" + rectNum + "]:first").remove();
}

function drawAllBlack() {
    let i = 0, len = rectangles.length;
    for (; i < len; i++) {
        if (rectangles[i].page === pageNumber && rectangles[i].marked === true && rectangles[i].drew === false) {
            drawBlackRec(rectangles[i].x, rectangles[i].y, rectangles[i].w, rectangles[i].h, i);
        }
    }
}

function drawAllRed() {
    let i = 0, len = rectangles.length;

    function drawRed() {
        drawRedRec(rectangles[i].x, rectangles[i].y, rectangles[i].w, rectangles[i].h, i);
    }

    for (; i < len; i++) {
        if (rectangles[i].page === pageNumber && rectangles[i].marked === false && rectangles[i].drew === false) {
            if ($("input[value='PESEL']").is(":checked") && rectangles[i].typeOfData === 2) {
                drawRed();
            }
            if ($("input[value='NIP']").is(":checked") && rectangles[i].typeOfData === 3) {
                drawRed();
            }
            if ($("input[value='REGON']").is(":checked") && rectangles[i].typeOfData === 4) {
                drawRed();
            }
            if ($("input[value='Name']").is(":checked") && rectangles[i].typeOfData === 5) {
                drawRed();
            }
            if ($("input[value='Phone_Number']").is(":checked") && rectangles[i].typeOfData === 6) {
                drawRed();
            }
            if ($("input[value='Address']").is(":checked") && rectangles[i].typeOfData === 7) {
                drawRed();
            }
            if ($("input[value='date']").is(":checked") && rectangles[i].typeOfData === 8) {
                drawRed();
            }
            if (rectangles[i].typeOfData === 1) {
                drawRed();
            }

        }
    }
}

$("#draw").on('click', function drawRects() {

    let c_page = $("canvas[id^=page]");
    let rectNum = -1;

    drawAllBlack();
    drawAllRed();

    let BB = c_page.get(0).getBoundingClientRect();
    let BBoffsetX = BB.left;
    let BBoffsetY = BB.top;

    function doCollide(rects, x, y) {
        let isCollision = false;
        rectNum = -1;
        let i = 0, len = rects.length;
        for (; i < len; i++) {
            let left = rects[i].x * SCALE_FIXED, right = rects[i].x * SCALE_FIXED + rects[i].w * SCALE_FIXED;
            let top = rects[i].y * SCALE_FIXED, bottom = rects[i].y * SCALE_FIXED + rects[i].h * SCALE_FIXED;
            if (right >= x
                && left <= x
                && bottom >= y
                && top <= y) {
                isCollision = rects[i];
                rectNum = i;
            }
        }
        return isCollision;
    }

    $('.textLayer').click(function (e) {
        $('.textLayer').hide();
        if (doCollide(rectangles, e.pageX - BBoffsetX, e.pageY - BBoffsetY)) {
            // console.log("RecNum " + rectNum);
            if (rectNum !== -1) {
                if (rectangles[rectNum].marked === false) {
                    rectangles[rectNum].marked = true;
                    removeRectangle(rectNum);
                    drawBlackRec(rectangles[rectNum].x, rectangles[rectNum].y, rectangles[rectNum].w, rectangles[rectNum].h, rectNum);
                } else if (rectangles[rectNum].marked === true) {
                    rectangles[rectNum].marked = false;
                    removeRectangle(rectNum);
                    drawRedRec(rectangles[rectNum].x, rectangles[rectNum].y, rectangles[rectNum].w, rectangles[rectNum].h, rectNum);
                }
            }
        }
        $('.textLayer').show();
    });
});
