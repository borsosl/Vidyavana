
import * as load from './load';

let mobile: boolean;

export function init() {
    const cs = client.system;
    mobile = cs.android || cs.ios || cs.iphone || cs.ipad || cs.winMobile;
    if(mobile) {
        $(window).on('swipeleft', function() {
            load.contextNext();
        }).on('swiperight', function() {
            load.contextPrev();
        });
    }
}

export function isMobile() {
    return mobile;
}
