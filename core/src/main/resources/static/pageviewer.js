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

// The workerSrc property shall be specified.
//
pdfjsLib.GlobalWorkerOptions.workerSrc =
    'pdfjs-dist/build/pdf.worker.js';

// Some PDFs need external cmaps.
//
var CMAP_URL = 'pdfjs-dist/cmaps/';
var CMAP_PACKED = true;

var PAGE_TO_VIEW = 1;
var SCALE = 1.0;
var SCALE_FIXED = SCALE / 0.75;
var number_of_pages;

var container = document.getElementById('pageContainer');
var pdf_prev = $("#pdf-prev");
var pdf_next = $("#pdf-next");


var loadingTask = pdfjsLib.getDocument({
    url: DEFAULT_URL,
    cMapUrl: CMAP_URL,
    cMapPacked: CMAP_PACKED
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

function render() {
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
            if (PAGE_TO_VIEW === number_of_pages) {
                pdf_next.prop("disabled", true);
            } else {
                pdf_next.prop("disabled", false);
            }


            return pdfPageView.draw();
        })
    });
}


render();
pdf_prev.prop("disabled", true);

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

$("#do-refactor").on('click', function () {

    var filteredRects = rects.filter(function (e) {
        return e.marked === true
    });
    console.log(filteredRects);
    $.ajax({
        url: "/api",
        type: 'post',
        data: JSON.stringify(filteredRects),
        contentType: 'application/json',
        error: function () {
            alert('error');
        }
    });
});

function drawRedRec(x, y, w, h, rectNum) {
    var $cloned = $("#pageContainer > div > div.canvasWrapper > canvas[id^=page]:last").first().clone();
    $cloned.clone().prop('id', 'rect' + rectNum).prop('style', 'position: absolute').prependTo("#pageContainer > div > div.canvasWrapper");
    var c = $("canvas[id^=rect]");
    var ctx = c.get(0).getContext("2d");
    ctx.strokeStyle = 'rgba(255,0,0,1)';
    ctx.beginPath();
    ctx.rect(SCALE_FIXED * x, SCALE_FIXED * y, SCALE_FIXED * w, SCALE_FIXED * h);
    ctx.stroke();
}

function drawBlackRec(x, y, w, h, rectNum) {
    var $cloned = $("#pageContainer > div > div.canvasWrapper > canvas[id^=page]:last").first().clone();
    $cloned.clone().prop('id', 'rect' + rectNum).prop('style', 'position: absolute').prependTo("#pageContainer > div > div.canvasWrapper");
    var c = $("canvas[id^=rect]");
    var ctx = c.get(0).getContext("2d");
    ctx.strokeStyle = 'rgba(0,0,0,1)';
    ctx.beginPath();
    ctx.rect(SCALE_FIXED * x, SCALE_FIXED * y, SCALE_FIXED * w, SCALE_FIXED * h);
    ctx.fillStyle = "black";
    ctx.fill();
}

function removeRec(rectNum) {
    $("#pageContainer > div > div.canvasWrapper > canvas[id=rect" + rectNum + "]:first").remove();
}

var rects = [];
for (var o in rectListJS) {
    rects.push(rectListJS[o]);
}

$("#draw").on('click', function () {

    var c_page = $("canvas[id^=page]");
    var rectNum = -1;

    function drawAllRed() {
        for (var i = 0, len = rects.length; i < len; i++) {
            if (rects[i].page === PAGE_TO_VIEW) {
                drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, i);
            }
        }
    }

    drawAllRed();
    var BB, BBoffsetX, BBoffsetY;
    setBB();

    function setBB() {
        BB = c_page.get(0).getBoundingClientRect();
        BBoffsetX = BB.left;
        BBoffsetY = BB.top;
    }

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
            console.log("RecNum " + rectNum);
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