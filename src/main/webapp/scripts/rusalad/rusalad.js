function render_rusalad_result(containerId, features, jobname, buildno, imagesUrl, appContext) {
    var container = document.getElementById(containerId);

    var header = document.createElement('h1');
    header.appendChild(document.createTextNode('Russian Salad test report for build ' + buildno));
    var historyLink = document.createElement('a');
    historyLink.setAttribute("href", "#history");
    historyLink.appendChild(document.createTextNode("history"));
    header.appendChild(document.createTextNode(" ("));
    header.appendChild(historyLink);
    header.appendChild(document.createTextNode(")"));
    container.appendChild(header);

    $(historyLink).fancybox({
        'hideOnContentClick':false,
        'overlayOpacity':.6,
        'zoomSpeedIn':400,
        'zoomSpeedOut':400,
        'frameWidth':200,
        'frameHeight':100,
        'centerOnScroll':false,
        'callbackOnShow':function () {
            render_rs_history("fancy_div", appContext + '/job/' + jobname + '/' + buildno + '/RSDynamic/History/', jobname, imagesUrl, appContext);
        }
    });

    var featureColCount = 1;

    for (var featureId = 0; featureId < features.length; featureId++) {
        var feature = features[featureId];

        var tf = document.createElement('table');
        tf.setAttribute('class', 'pane');
        var tfb = document.createElement('tbody');
        var tfhr = document.createElement('tr');
        var tfhc = document.createElement('td');
        tfhc.appendChild(document.createTextNode(feature.featureName));
        tfhc.setAttribute('colspan', featureColCount);
        tfhc.setAttribute('class', 'pane-header');
        tfhr.appendChild(tfhc);
        tfb.appendChild(tfhr);

        for (var scenarioId = 0; scenarioId < feature.scenarios.length; scenarioId++) {
            var scenario = feature.scenarios[scenarioId];
            var scenarioSuffix = featureId + '_' + scenarioId;
            var detailId = containerId + '_scenario_' + scenarioSuffix;
            var tfbr = document.createElement('tr');

            var tfbc = document.createElement('td');

            // Prepare scenario digest
            var digest = document.createElement('div');
            digest.setAttribute('style', 'display: block; width: 100%; text-align: left; cursor: pointer; text-decoration: underline;');
            digest.setAttribute('onclick', 'expand_scenario("' + detailId + '")');
            digest.setAttribute('id', detailId + '_digest1');

            var tfbi = document.createElement('img');
            var scenarioImgName = scenario.passed ? 'blue' : 'yellow';
            if (scenario.status == 'undefined') {
                scenarioImgName = 'red';
            }
            tfbi.setAttribute('src', imagesUrl + '/16x16/' + scenarioImgName + '.png');
            tfbi.setAttribute('width', '16');
            tfbi.setAttribute('height', '16');
            digest.appendChild(tfbi);

            digest.appendChild(document.createTextNode(scenario.scenarioName));

            // Prepare scenario digest (expanded)
            var digest2 = document.createElement('div');
            digest2.setAttribute('style', 'display: none; width: 100%; text-align: left; cursor: pointer; text-decoration: underline;');
            digest2.setAttribute('onclick', 'expand_scenario("' + detailId + '")');
            digest2.setAttribute('id', detailId + '_digest2');

            var tfbi2 = document.createElement('img');
            tfbi2.setAttribute('src', imagesUrl + '/16x16/' + scenarioImgName + '.png');
            tfbi2.setAttribute('width', '16');
            tfbi2.setAttribute('height', '16');
            digest2.appendChild(tfbi2);

            digest2.appendChild(document.createTextNode(scenario.scenarioName + ' (click to collapse)'));

            // Prepare scenario detail
            var detail = document.createElement('div');
            detail.setAttribute('style', 'display: none; padding-left: 2em;');
            detail.setAttribute('id', detailId);

            var ts = document.createElement('table');
            ts.setAttribute('class', 'pane');
            ts.setAttribute('style', 'border: 0;');
            var tsb = document.createElement('tbody');

            for (var stepId = 0; stepId < scenario.steps.length; stepId++) {
                var step = scenario.steps[stepId];
                var tsr = document.createElement('tr');

                var imgName;
                switch (step.status) {
                    case "passed":
                        imgName = 'blue';
                        break;
                    case "unknown":
                        imgName = 'red';
                        break;
                    case "skipped":
                        imgName = 'grey';
                        break;
                    case "failed":
                        imgName = 'yellow';
                        break;
                    default:
                        imgName = 'red';
                }
                var stepImg = document.createElement('img');
                stepImg.setAttribute('src', imagesUrl + '/16x16/' + imgName + '.png');
                stepImg.setAttribute('width', '16');
                stepImg.setAttribute('height', '16');

                var tsc1 = document.createElement('td');
                tsc1.appendChild(stepImg);
                var formattedArgs = step.args.replace(/&lt;arg>(.*?)&lt;\/arg>/g, "<b>$1</b>");
                tsc1.innerHTML = tsc1.innerHTML + step.keyword + formattedArgs;
                if (step.exception != null) {
                    var exceptionDiv = document.createElement('div');
                    exceptionDiv.setAttribute('style', 'color:red;display:block;padding-left:2em;');
                    exceptionDiv.appendChild(document.createTextNode('Error: ' + step.exception));
                    tsc1.appendChild(exceptionDiv);
                }

                tsr.appendChild(tsc1);
                tsb.appendChild(tsr);
            }

            if (scenario.videoFile) {
                var tvr = document.createElement('tr');
                var tvc1 = document.createElement('td');
                tvc1.appendChild(document.createTextNode('Video report available: '));
                var tva = document.createElement('a');
                var videoName = appContext + '/job/' + jobname + '/' + buildno + '/RSDynamic/Files/' + feature.safeName + '/' + scenario.videoFile;
                $(tva).hover(function () {
                    videoName = $(this).attr('href').replace('.mkv#', '');
                });
                tva.setAttribute("href", videoName + ".mkv#");
                tva.appendChild(document.createTextNode(scenario.videoFile));
                $(tva).fancybox({
                    'hideOnContentClick':false,
                    'overlayOpacity':.6,
                    'zoomSpeedIn':400,
                    'zoomSpeedOut':400,
                    'callbackOnShow':function () {
                        player = $f("fancy_div", appContext + '/plugin/jenkins-rusalad-plugin/flowplayer/flowplayer-3.2.7.swf', {
                            play:{opacity:0},
                            clip:{
                                autoPlay:true,
                                autoBuffering:true,
                                url:videoName + ".flv",
                                captionUrl:videoName + ".xml",
                                onStart:function (clip) {
                                    var wrap = jQuery(this.getParent());
                                    var clipwidth = clip.metaData.width;
                                    var clipheight = clip.metaData.height;
                                    if (clipwidth > 800) {
                                        clipheight = clipheight * 800 / clipwidth;
                                        clipwidth = 800;
                                    }
                                    var pos = $.fn.fancybox.getViewport();
                                    $("#fancy_outer").css({width:clipwidth, height:clipheight});
                                    $("#fancy_outer").css('left', ((clipwidth + 36) > pos[0] ? pos[2] : pos[2] + Math.round((pos[0] - clipwidth - 36) / 2)));
                                    $("#fancy_outer").css('top', ((clipheight + 50) > pos[1] ? pos[3] : pos[3] + Math.round((pos[1] - clipheight - 50) / 2)));
                                }
                            },
                            plugins:{
                                captions:{
                                    url:appContext + '/plugin/jenkins-rusalad-plugin/flowplayer/flowplayer.captions-3.2.3.swf',
                                    captionTarget:'content'
                                },
                                content:{
                                    url:appContext + '/plugin/jenkins-rusalad-plugin/flowplayer/flowplayer.content-3.2.0.swf',
                                    bottom:5,
                                    height:40,
                                    backgroundColor:'transparent',
                                    backgroundGradient:'none',
                                    border:0,
                                    textDecoration:'outline',
                                    style:{
                                        'body':{
                                            fontSize:'14',
                                            fontFamily:'Arial',
                                            textAlign:'center',
                                            color:'#ffffff'
                                        }
                                    }
                                }
                            }
                        });
                        player.load();
                        $('#fancy_close').click(function () {
                            $("#fancy_div_api").remove();
                        });
                    },
                    'callbackOnClose':function () {
                        $("#fancy_div_api").remove();
                    }
                });
                tvc1.appendChild(tva);

                tvr.appendChild(tvc1);
                tsb.appendChild(tvr);
            }

            ts.appendChild(tsb);
            detail.appendChild(ts);

            tfbc.appendChild(digest);
            tfbc.appendChild(digest2);
            tfbc.appendChild(detail);
            tfbr.appendChild(tfbc);
            tfb.appendChild(tfbr);
        }

        tf.appendChild(tfb);
        container.appendChild(tf);
    }
}

function expand_scenario(detailId) {
    if (document.getElementById(detailId).style.display == 'block') {
        document.getElementById(detailId).style.display = 'none';
    } else {
        document.getElementById(detailId).style.display = 'block';
    }
    if (document.getElementById(detailId + '_digest1').style.display == 'block') {
        document.getElementById(detailId + '_digest1').style.display = 'none';
        document.getElementById(detailId + '_digest2').style.display = 'block';
    } else {
        document.getElementById(detailId + '_digest1').style.display = 'block';
        document.getElementById(detailId + '_digest2').style.display = 'none';
    }
}

function render_rs_history(elementId, historyUrl, jobname, imagesUrl, appContext) {
    var container = document.getElementById(elementId);

    var spinnerDiv = document.createElement('div');
    spinnerDiv.setAttribute('style', 'display:block;text-align: center;color:black; margin:40px 0;');
    var spinner = document.createElement('img');
    spinner.setAttribute('src', imagesUrl + '/spinner.gif');
    spinner.setAttribute('width', '16');
    spinner.setAttribute('height', '16');
    spinnerDiv.appendChild(spinner);
    spinnerDiv.appendChild(document.createTextNode(" Loading..."));
    container.innerHTML = '';
    container.appendChild(spinnerDiv);

    $.getJSON(historyUrl, null,
        function (data, textStatus, jqXHR) {
            container.innerHTML = '';
            if (data == null) {
                container.appendChild(document.createTextNode("No build history found"));
                return;
            }
            var table = document.createElement('table');
            table.setAttribute("cellspacing", "0");
            table.setAttribute("cellpadding", "2");
            table.setAttribute("class", "historyTable");
            var tbody = document.createElement('tbody');
            var trbuilds = document.createElement('tr');
            var tdbuildsmargin = document.createElement('td');
            tdbuildsmargin.setAttribute("colspan", "2");
            tdbuildsmargin.setAttribute("class", "historyBuildMargin");
            tdbuildsmargin.appendChild(document.createTextNode("Build #"));
            trbuilds.appendChild(tdbuildsmargin);
            for (var buildIdx = 0; buildIdx < data.builds.length; buildIdx++) {
                var buildNo = data.builds[buildIdx];
                var tdbuild = document.createElement('td');
                tdbuild.setAttribute("width", "32");
                tdbuild.setAttribute("align", "center");
                tdbuild.setAttribute("class", "historyBuildNo".concat(buildIdx & 1));
                var buildLink = document.createElement('a');
                buildLink.setAttribute("href", appContext + '/job/' + jobname + '/' + buildNo);
                buildLink.appendChild(document.createTextNode(buildNo));
                tdbuild.appendChild(buildLink);
                trbuilds.appendChild(tdbuild);
            }
            tbody.appendChild(trbuilds);

            var overallIdx = 0;
            for (var featureIdx = 0; featureIdx < data.featureNames.length; featureIdx++) {
                var featureName = data.featureNames[featureIdx];
                var feature = data.features[featureName];
                var trscenario = document.createElement('tr');
                var tdfeaturename = document.createElement('td');
                tdfeaturename.setAttribute("rowspan", feature.scenarioNames.length);
                tdfeaturename.setAttribute("valign", "middle");
                tdfeaturename.setAttribute("align", "center");
                tdfeaturename.setAttribute("class", "historyFeatureName".concat(featureIdx & 1));
                tdfeaturename.appendChild(document.createTextNode(featureName));

                trscenario.appendChild(tdfeaturename);
                for (var scenarioIdx = 0; scenarioIdx < feature.scenarioNames.length; scenarioIdx++) {
                    overallIdx++;
                    var scenarioName = feature.scenarioNames[scenarioIdx];
                    var scenario = feature.scenarios[scenarioName];

                    if (trscenario == null) {
                        trscenario = document.createElement('tr');
                    }

                    var tdscenarioname = document.createElement('td');
                    tdscenarioname.setAttribute("class", "historyScenarioName".concat(overallIdx & 1));
                    tdscenarioname.setAttribute("valign", "middle");
                    tdscenarioname.setAttribute("align", "center");
                    tdscenarioname.appendChild(document.createTextNode(scenarioName));
                    trscenario.appendChild(tdscenarioname);

                    for (var buildIdx2 = 0; buildIdx2 < data.builds.length; buildIdx2++) {
                        var buildNo2 = data.builds[buildIdx2];
                        var bullet = document.createElement('img');
                        var imgName;
                        var alt;
                        switch (scenario[buildNo2]) {
                            case "passed":
                                imgName = 'blue';
                                alt = 'Passed';
                                break;
                            case "failed":
                                imgName = 'yellow';
                                alt = 'Failed';
                                break;
                            case "undefined":
                                imgName = 'red';
                                alt = 'Has undefined steps';
                                break;
                            default:
                                alt = 'Did not run';
                                imgName = 'empty';
                        }
                        bullet.setAttribute('src', imagesUrl + '/16x16/' + imgName + '.png');
                        bullet.setAttribute('width', '16');
                        bullet.setAttribute('height', '16');
                        bullet.setAttribute('alt', alt);
                        var tdbullet = document.createElement('td');
                        tdbullet.setAttribute("align", "center");
                        tdbullet.setAttribute("class", "historyBulletCell".concat(buildIdx2 & 1, overallIdx & 1));
                        tdbullet.appendChild(bullet);
                        trscenario.appendChild(tdbullet);
                    }
                    tbody.appendChild(trscenario);
                    trscenario = null;
                }
            }

            table.appendChild(tbody);
            container.appendChild(table);
            container.style.color = "black";

            var clipwidth = table.scrollWidth + 36;
            var clipheight = table.scrollHeight + 24;
            if (clipwidth > 800) {
                clipheight = clipheight * 800 / clipwidth;
                clipwidth = 800;
            }
            var pos = $.fn.fancybox.getViewport();
            $("#fancy_outer").css({width:clipwidth, height:clipheight});
            $("#fancy_outer").css('left', ((clipwidth + 36) > pos[0] ? pos[2] : pos[2] + Math.round((pos[0] - clipwidth - 36) / 2)));
            $("#fancy_outer").css('top', ((clipheight + 50) > pos[1] ? pos[3] : pos[3] + Math.round((pos[1] - clipheight - 50) / 2)));
        }).error(function (jqXHR, textStatus, errorThrown) {
            container.innerHTML = '';
            container.appendChild(document.createTextNode("Failed to get build history: " + textStatus))
            container.appendChild(document.createTextNode(errorThrown))
        });
}
