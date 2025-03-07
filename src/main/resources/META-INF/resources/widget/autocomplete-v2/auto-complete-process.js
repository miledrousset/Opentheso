
let autoComplete = (function () {

    function autoComplete(options) {

        if (!document.querySelector)
            return;

        function hasClass(el, className) {
            return el.classList ? el.classList.contains(className) : new RegExp('\\b' + className + '\\b').test(el.className);
        }

        function addEvent(el, type, handler) {
            if (el.attachEvent)
                el.attachEvent('on' + type, handler);
            else
                el.addEventListener(type, handler);
        }

        function removeEvent(el, type, handler) {
            if (el.detachEvent)
                el.detachEvent('on' + type, handler);
            else
                el.removeEventListener(type, handler);
        }

        function live(elClass, event, cb, context) {
            addEvent(context || document, event, function (e) {
                let found, el = e.target || e.srcElement;
                while (el && !(found = hasClass(el, elClass)))
                    el = el.parentElement;
                if (found)
                    cb.call(el, e);
            });
        }
        
        const data = new Map();
        const output = document.getElementById(options['output']);

        let o = {
            selector: 0,
            minChars: 2,
            delay: 150,
            offsetLeft: 0,
            offsetTop: 1,
            cache: 1,
            menuClass: '',

            source: function (term, suggest) {

                if (document.getElementById(options['selector'].substring(1)).value.length >= o.minChars) {
                    document.getElementById(options['loading']).style.visibility = 'visible';
                } else {
                    document.getElementById(options['loading']).style.visibility = 'hidden';
                }

                let urlWS = options.url + options.thesaurus + '/autocomplete/' + term + '?';
                if (options['groupe'] !== undefined) {
                    urlWS += 'group=' + options['groupe'] + '&';
                }
                if (options['lang'] !== undefined) {
                    urlWS += 'lang=' + options['lang'] + '&';
                }
                urlWS += 'full=true';
                console.log('>> ' + urlWS);

                $.ajax({
                    url: urlWS,
                    type: 'GET',
                    dataType: 'html',
                    success: function (arrayOfObjects) {
                        while (output.firstChild) {
                            output.removeChild(output.firstChild);
                        }

                        let suggestions = [];
                        arrayOfObjects = JSON.parse(arrayOfObjects);
                        for (let i = 0; i < arrayOfObjects.length; i++) {
                            suggestions.push(arrayOfObjects[i]);
                        }
                        suggest(suggestions);
                        document.getElementById(options['loading']).style.visibility = 'hidden';
                    },
                    error: function (resultat, statut, erreur) {
                        document.getElementById(options['loading']).style.visibility = 'hidden';

                        while (output.firstChild) {
                            output.removeChild(output.firstChild);
                        }
                        output.innerHTML = '<p style="color: red; font-size: 13px">Aucune données trouvées...</p>';
                    }
                })
            },

            renderItem: function (item, search) {
                search = search.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
                let re = new RegExp("(" + search.split(' ').join('|') + ")", "gi");
                data.set(item.label, item);

                let sizeText = '';
                if (item.isAltLabel === true) {
                    sizeText = 'font-style: italic; text-decoration: underline;';
                }

                return '<div class="autocomplete-suggestion" style="' + sizeText + '" data-val="' + item.label + '">'
                    + item.label.replace(re, "<b>$1</b>") + '</div>';
            },
            onSelect: function (e, term, item) {}
        };

        for (let k in options) {
            if (options.hasOwnProperty(k))
                o[k] = options[k];
        }

        // init
        let elems = typeof o.selector == 'object' ? [o.selector] : document.querySelectorAll(o.selector);
        for (let i = 0; i < elems.length; i++) {
            let that = elems[i];
            that.sc = document.createElement('div');
            that.sc.className = 'autocomplete-suggestions ' + o.menuClass;
            that.autocompleteAttr = that.getAttribute('autocomplete');
            that.setAttribute('autocomplete', 'off');
            that.cache = {};
            that.last_val = '';

            that.updateSC = function (resize, next) {
                let rect = that.getBoundingClientRect();
                that.sc.style.left = Math.round(rect.left + (window.pageXOffset || document.documentElement.scrollLeft) + o.offsetLeft) + 'px';
                that.sc.style.top = Math.round(rect.bottom + (window.pageYOffset || document.documentElement.scrollTop) + o.offsetTop) + 'px';
                that.sc.style.width = Math.round(rect.right - rect.left) + 'px'; // outerWidth
                if (!resize) {
                    that.sc.style.display = 'block';
                    if (!that.sc.maxHeight) {
                        that.sc.maxHeight = parseInt((window.getComputedStyle ? getComputedStyle(that.sc, null) : that.sc.currentStyle).maxHeight);
                    }
                    if (!that.sc.suggestionHeight)
                        that.sc.suggestionHeight = that.sc.querySelector('.autocomplete-suggestion').offsetHeight;
                    if (that.sc.suggestionHeight)
                        if (!next)
                            that.sc.scrollTop = 0;
                        else {
                            let scrTop = that.sc.scrollTop, selTop = next.getBoundingClientRect().top - that.sc.getBoundingClientRect().top;
                            if (selTop + that.sc.suggestionHeight - that.sc.maxHeight > 0)
                                that.sc.scrollTop = selTop + that.sc.suggestionHeight + scrTop - that.sc.maxHeight;
                            else if (selTop < 0)
                                that.sc.scrollTop = selTop + scrTop;
                        }
                }
            }
            addEvent(window, 'resize', that.updateSC);
            document.body.appendChild(that.sc);

            live('autocomplete-suggestion', 'mouseleave', function (e) {
                let sel = that.sc.querySelector('.autocomplete-suggestion.selected');
                if (sel)
                    setTimeout(function () {
                        sel.className = sel.className.replace('selected', '');
                    }, 20);
            }, that.sc);

            live('autocomplete-suggestion', 'mouseover', function (e) {
                let sel = that.sc.querySelector('.autocomplete-suggestion.selected');
                if (sel)
                    sel.className = sel.className.replace('selected', '');
                this.className += ' selected';
            }, that.sc);

            live('autocomplete-suggestion', 'mousedown', function (e) {
                let element = document.getElementById(options['loading']);
                element.style.visibility = 'hidden';
                
                if (hasClass(this, 'autocomplete-suggestion')) { // else outside click
                    let v = this.getAttribute('data-val');
                    that.value = v;
                    o.onSelect(e, v, this);
                    that.sc.style.display = 'none';
                }
            }, that.sc);


            that.blurHandler = function () {
                document.getElementById(options['loading']).style.visibility = 'hidden';

                if (data.get(that.value)) {
                    while (output.firstChild) {
                        output.removeChild(output.firstChild);
                    }

                    if (data.get(that.value).definition && data.get(that.value).definition.length > 3) {
                        let paragraphe1 = document.createElement("p");
                        paragraphe1.appendChild(document.createTextNode('Définition : '));
                        paragraphe1.style.textAlign = "justify";
                        paragraphe1.style.color = "#f47b2a";
                        output.appendChild(paragraphe1);

                        let paragraphe2 = document.createElement("p");
                        paragraphe2.appendChild(document.createTextNode(data.get(that.value).definition));
                        paragraphe2.style.textAlign = "justify";
                        output.appendChild(paragraphe2);
                    }

                    // Creation d'un lien (<a>) et définition de ses attributs
                    let monLien = document.createElement("a");
                    monLien.href = data.get(that.value).uri;
                    monLien.target = "_blank";
                    monLien.textContent = 'Lien vers le concept';
                    output.appendChild(monLien);

                    if (!document.querySelector('.autocomplete-suggestions:hover')) {
                        that.last_val = that.value;
                        that.sc.style.display = 'none';
                        setTimeout(function () { that.sc.style.display = 'none';}, 350);
                    } else if (that !== document.activeElement) {
                        setTimeout(function () { that.focus();}, 20);
                    }
                }
            };
            addEvent(that, 'blur', that.blurHandler);

            let suggest = function (data) {
                that.cache[that.value] = data;
                if (data.length && that.value.length >= o.minChars) {
                    let str = '';
                    for (let i = 0; i < data.length; i++)
                        str += o.renderItem(data[i], that.value);
                    that.sc.innerHTML = str;
                    that.updateSC(0);
                } else
                    that.sc.style.display = 'none';
            }

            that.keydownHandler = function (e) {
                let key = window.event ? e.keyCode : e.which;
                if ((key == 40 || key == 38) && that.sc.innerHTML) {
                    let next, sel = that.sc.querySelector('.autocomplete-suggestion.selected');
                    if (!sel) {
                        next = (key == 40) ? that.sc.querySelector('.autocomplete-suggestion') : that.sc.childNodes[that.sc.childNodes.length - 1]; // first : last
                        next.className += ' selected';
                        that.value = next.getAttribute('data-val');
                    } else {
                        next = (key == 40) ? sel.nextSibling : sel.previousSibling;
                        if (next) {
                            sel.className = sel.className.replace('selected', '');
                            next.className += ' selected';
                            that.value = next.getAttribute('data-val');
                        } else {
                            sel.className = sel.className.replace('selected', '');
                            that.value = that.last_val;
                            next = 0;
                        }
                    }
                    that.updateSC(0, next);
                    return false;
                } else if (key == 27) {
                    that.value = that.last_val;
                    that.sc.style.display = 'none';
                } else if (key == 13 || key == 9) {
                    let sel = that.sc.querySelector('.autocomplete-suggestion.selected');
                    if (sel && that.sc.style.display != 'none') {
                        o.onSelect(e, sel.getAttribute('data-val'), sel);
                        setTimeout(function () {
                            that.sc.style.display = 'none';
                        }, 20);
                    }
                }
            };
            addEvent(that, 'keydown', that.keydownHandler);

            that.keyupHandler = function (e) {
                let key = window.event ? e.keyCode : e.which;
                if (!key || (key < 35 || key > 40) && key != 13 && key != 27) {
                    let val = that.value;
                    if (val.length >= o.minChars) {
                        if (val != that.last_val) {
                            that.last_val = val;
                            clearTimeout(that.timer);
                            if (o.cache) {
                                if (val in that.cache) {
                                    suggest(that.cache[val]);
                                    return;
                                }
                                for (let i = 1; i < val.length - o.minChars; i++) {
                                    let part = val.slice(0, val.length - i);
                                    if (part in that.cache && !that.cache[part].length) {
                                        suggest([]);
                                        return;
                                    }
                                }
                            }
                            that.timer = setTimeout(function () {o.source(val, suggest)}, o.delay);
                        }
                    } else {
                        that.last_val = val;
                        that.sc.style.display = 'none';
                    }
                }
            };
            addEvent(that, 'keyup', that.keyupHandler);

            that.focusHandler = function (e) {
                that.last_val = '\n';
                that.keyupHandler(e)
            };
            if (!o.minChars)
                addEvent(that, 'focus', that.focusHandler);
        }

        // public destroy method
        this.destroy = function () {
            for (let i = 0; i < elems.length; i++) {
                let that = elems[i];
                removeEvent(window, 'resize', that.updateSC);
                removeEvent(that, 'blur', that.blurHandler);
                removeEvent(that, 'focus', that.focusHandler);
                removeEvent(that, 'keydown', that.keydownHandler);
                removeEvent(that, 'keyup', that.keyupHandler);
                if (that.autocompleteAttr)
                    that.setAttribute('autocomplete', that.autocompleteAttr);
                else
                    that.removeAttribute('autocomplete');
                document.body.removeChild(that.sc);
                that = null;
            }
        };
    }
    return autoComplete;
})();

(function () {
    if (typeof define === 'function' && define.amd)
        define('autoComplete', function () {
            return autoComplete;
        });
    else if (typeof module !== 'undefined' && module.exports)
        module.exports = autoComplete;
    else
        window.autoComplete = autoComplete;
})();
