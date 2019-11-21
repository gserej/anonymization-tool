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

var CMAP_URL = 'pdfjs-dist/web/cmaps/';
var CMAP_PACKED = true;

var PAGE_TO_VIEW = 1;
var SCALE = 1.0;
var SCALE_FIXED = SCALE / 0.75;
var number_of_pages;

var container = document.getElementById('pageContainer');
var pdf_prev = $("#pdf-prev");
var pdf_next = $("#pdf-next");

pdf_prev.on('click', function () {
    if (PAGE_TO_VIEW !== 1) {
        PAGE_TO_VIEW--;
        render();
        disableEnablePrevNext();
    }
});

pdf_next.on('click', function () {
    if (PAGE_TO_VIEW !== number_of_pages) {
        PAGE_TO_VIEW++;
        render();
        disableEnablePrevNext();
    }
});

function disableEnablePrevNext() {
    if (PAGE_TO_VIEW === 1) {
        pdf_prev.prop("disabled", true);
    } else {
        pdf_prev.prop("disabled", false);
    }
    if (PAGE_TO_VIEW === number_of_pages) {
        pdf_next.prop("disabled", true);
    } else {
        pdf_next.prop("disabled", false);
    }
}

disableEnablePrevNext();

var DEFAULT_URL;
var message;

$.ajax({
    type: "get",
    url: '/api/files',
    success: function (data) {
        if (data !== "") {
            DEFAULT_URL = data;
            $("#file-link").attr("href", data).html(data);
            render();
        }
    }
});

function fetchMessage() {
    $.ajax({
        type: "get",
        url: '/api/message',
        success: function (data) {
            if (data !== "") {
                message = data;
                $("#message").html(message);
            }
        }
    });
}

fetchMessage();

$("#do-refactor").on('click', function () {

    var filteredRects = rects.filter(function (e) {
        return e.marked === true
    });
    // console.log(filteredRects);
    $.ajax({
        url: "/api/rectangles",
        method: 'POST',
        data: JSON.stringify(filteredRects),
        contentType: 'application/json',
        success: function () {
            fetchMessage();
        }
    })
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
    });
var rects = [];

function render() {
    var loadingTask = pdfjsLib.getDocument({
        url: DEFAULT_URL,
        cMapUrl: CMAP_URL,
        cMapPacked: CMAP_PACKED
    });
    $(".page").remove();
    loadingTask.promise.then(function (pdfDocument) {
        number_of_pages = pdfDocument.numPages;
        document.getElementById('page_count').textContent = number_of_pages;
        document.getElementById('page_num').textContent = PAGE_TO_VIEW;
        return pdfDocument.getPage(PAGE_TO_VIEW).then(function (pdfPage) {
            var pdfPageView = new pdfjsViewer.PDFPageView({
                container: container,
                id: PAGE_TO_VIEW,
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
            if (PAGE_TO_VIEW === number_of_pages) {
                pdf_next.prop("disabled", true);
            } else {
                pdf_next.prop("disabled", false);
            }

            for (var i in rects) {
                if (rects.hasOwnProperty(i)) {
                    rects[i].drew = false;
                }
            }

            startAjaxGet();

            return pdfPageView.draw();
        })
    });
}


var startAjaxGet = (function () {
    var executed = false;
    return function () {
        if (!executed) {
            executed = true;
            getRectangles();
        }
    };
})();

function hasId(prop, value, data) {
    return data.some(function (obj) {
        return prop in obj && obj[prop] === value;
    });
}

function getRectangles() {
    $.ajax({
        type: "get",
        url: "/api/rectangles",
        contentType: 'application/json',
        success: function (data) {
            if (!$.trim(data)) {
                console.log("No new data, trying again in 1 second.");
                setTimeout(function () {
                    getRectangles();
                }, 1000)
            } else {
                var additionalRectListJS = JSON.stringify(data);
                for (var o in data) {
                    if (data.hasOwnProperty(o)) {
                        if (!hasId('id', data[o].id, rects)) {

                            rects.push(data[o]);
                            // console.log("Object pushed: " + JSON.stringify(data[o]));
                        }
                    }
                }
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


function drawRedRec(x, y, w, h, rectNum) {
    var $cloned = $("#pageContainer > div > div.canvasWrapper > canvas[id^=page]:last").first().clone();
    $cloned.clone().prop('id', 'rect' + rectNum).prop('style', 'position: absolute')
        .removeAttr("moz-opaque").prependTo("#pageContainer > div > div.canvasWrapper");
    var c = $("canvas[id^=rect]");
    var ctx = c.get(0).getContext("2d");
    ctx.strokeStyle = 'rgba(255,0,0,1)';
    ctx.beginPath();
    ctx.rect(SCALE_FIXED * x, SCALE_FIXED * y, SCALE_FIXED * w, SCALE_FIXED * h);
    ctx.stroke();
    rects[rectNum].drew = true;
    // console.log("Red rectangle has been drawn: " + rectNum);
}

function drawBlackRec(x, y, w, h, rectNum) {
    var $cloned = $("#pageContainer > div > div.canvasWrapper > canvas[id^=page]:last").first().clone();
    $cloned.clone().prop('id', 'rect' + rectNum).prop('style', 'position: absolute')
        .removeAttr("moz-opaque").prependTo("#pageContainer > div > div.canvasWrapper");
    var c = $("canvas[id^=rect]");
    var ctx = c.get(0).getContext("2d");
    ctx.strokeStyle = 'rgba(0,0,0,1)';
    ctx.beginPath();
    ctx.rect(SCALE_FIXED * x, SCALE_FIXED * y, SCALE_FIXED * w, SCALE_FIXED * h);
    ctx.fillStyle = "black";
    ctx.fill();
}

$("#getMoreBoxes").on('click', function () {
    getRectangles();
});

function removeRec(rectNum) {
    $("#pageContainer > div > div.canvasWrapper > canvas[id=rect" + rectNum + "]:first").remove();
}

function drawAllBlack() {
    for (var i = 0, len = rects.length; i < len; i++) {
        if (rects[i].page === PAGE_TO_VIEW) {
            if (rects[i].marked === true) {
                if (rects[i].drew === false) {
                    drawBlackRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                }
            }
        }
    }
}

function drawAllRed() {
    for (var i = 0, len = rects.length; i < len; i++) {
        if (rects[i].page === PAGE_TO_VIEW) {
            if (rects[i].marked === false) {
                if (rects[i].drew === false) {
                    if ($("input[value='PESEL']").is(":checked")) {
                        if (rects[i].typeOfData === 2) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if ($("input[value='NIP']").is(":checked")) {
                        if (rects[i].typeOfData === 3) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if ($("input[value='REGON']").is(":checked")) {
                        if (rects[i].typeOfData === 4) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if ($("input[value='Name']").is(":checked")) {
                        if (rects[i].typeOfData === 5) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if ($("input[value='Phone_Number']").is(":checked")) {
                        if (rects[i].typeOfData === 6) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if ($("input[value='Address']").is(":checked")) {
                        if (rects[i].typeOfData === 7) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if ($("input[value='date']").is(":checked")) {
                        if (rects[i].typeOfData === 8) {
                            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                        }
                    }
                    if (rects[i].typeOfData === 1) {
                        drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
                    }

                }
            }
        }
    }
}

$("#draw").on('click', function drawRects() {

    var c_page = $("canvas[id^=page]");
    var rectNum = -1;

    drawAllBlack();
    drawAllRed();
    var BB, BBoffsetX, BBoffsetY;

    function setBB() {
        BB = c_page.get(0).getBoundingClientRect();
        BBoffsetX = BB.left;
        BBoffsetY = BB.top;
    }

    setBB();

    function collides(rects, x, y) {
        var isCollision = false;
        rectNum = -1;
        for (var i = 0, len = rects.length; i < len; i++) {
            var left = rects[i].x * SCALE_FIXED, right = rects[i].x * SCALE_FIXED + rects[i].w * SCALE_FIXED;
            var top = rects[i].y * SCALE_FIXED, bottom = rects[i].y * SCALE_FIXED + rects[i].h * SCALE_FIXED;
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
        if (collides(rects, e.pageX - BBoffsetX, e.pageY - BBoffsetY)) {
            // console.log("RecNum " + rectNum);
            if (rectNum !== -1) {
                if (rects[rectNum].marked === false) {
                    rects[rectNum].marked = true;
                    removeRec(rectNum);
                    drawBlackRec(rects[rectNum].x, rects[rectNum].y, rects[rectNum].w, rects[rectNum].h, rectNum);
                } else if (rects[rectNum].marked === true) {
                    rects[rectNum].marked = false;
                    removeRec(rectNum);
                    drawRedRec(rects[rectNum].x, rects[rectNum].y, rects[rectNum].w, rects[rectNum].h, rectNum);
                }
            }
        }
        $('.textLayer').show();
    });
});
