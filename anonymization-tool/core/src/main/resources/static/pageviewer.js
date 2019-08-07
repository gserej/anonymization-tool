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
var number_pages;

var container = document.getElementById('pageContainer');

var loadingTask = pdfjsLib.getDocument({
    url: DEFAULT_URL,
    cMapUrl: CMAP_URL,
    cMapPacked: CMAP_PACKED
});

// Loading document.
function render() {
    $(".page").remove();

    loadingTask.promise.then(function (pdfDocument) {
        number_pages = pdfDocument.numPages;
        document.getElementById('page_count').textContent = number_pages;
        document.getElementById('page_num').textContent = PAGE_TO_VIEW;
        return pdfDocument.getPage(PAGE_TO_VIEW).then(function (pdfPage) {
            var pdfPageView = new pdfjsViewer.PDFPageView({
                container: container,
                id: PAGE_TO_VIEW,
                scale: SCALE,
                defaultViewport: pdfPage.getViewport({scale: SCALE}),
                textLayerFactory: new pdfjsViewer.DefaultTextLayerFactory(),
                // annotationLayerFactory: new pdfjsViewer.DefaultAnnotationLayerFactory(),
            });
            pdfPageView.setPdfPage(pdfPage);
            return pdfPageView.draw();
        });
    });
}

render();

$("#pdf-prev").on('click', function () {
    if (PAGE_TO_VIEW !== 1) {
        PAGE_TO_VIEW--;
        render();
    }
});

$("#pdf-next").on('click', function () {
    if (PAGE_TO_VIEW !== number_pages) {
        PAGE_TO_VIEW++;
        render();
    }
});

$("#draw2").on('click', function () {
    $.ajax({
        url: "/api",
        type: 'post',
        dataType: 'json',
        data: JSON.stringify(rects),
        contentType: 'application/json',
        success: function () {
            alert("success");
        },
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
    ctx.rect(x, y, w, h);
    ctx.stroke();
}

function drawBlackRec(x, y, w, h, rectNum) {
    var $cloned = $("#pageContainer > div > div.canvasWrapper > canvas[id^=page]:last").first().clone();
    $cloned.clone().prop('id', 'rect' + rectNum).prop('style', 'position: absolute').prependTo("#pageContainer > div > div.canvasWrapper");
    var c = $("canvas[id^=rect]");
    var ctx = c.get(0).getContext("2d");
    ctx.strokeStyle = 'rgba(0,0,0,1)';
    ctx.beginPath();
    ctx.rect(x, y, w, h);
    ctx.fillStyle = "black";
    ctx.fill();
}

function removeRec(rectNum) {
    $("#pageContainer > div > div.canvasWrapper > canvas[id=rect" + rectNum + "]:first").remove();
}

var rects = [{id: 0, marked: false, x: 10, y: 10, w: 50, h: 20},
    {id: 1, marked: false, x: 75, y: 75, w: 50, h: 20},
    {id: 2, marked: false, x: 150, y: 150, w: 40, h: 20}
];
$("#draw").on('click', function () {
    var c_page = $("canvas[id^=page]");
    var rectNum = -1;

    function drawAllRed() {
        for (var i = 0, len = rects.length; i < len; i++) {
            drawRedRec(rects[i].x, rects[i].y, rects[i].w, rects[i].h, rects[i].id);
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
            var left = rects[i].x, right = rects[i].x + rects[i].w;
            var top = rects[i].y, bottom = rects[i].y + rects[i].h;
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
        if (collides(rects, e.clientX - BBoffsetX, e.clientY - BBoffsetY)) {
            console.log("RecNum " + rectNum);
            if (rectNum >= 0) {
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