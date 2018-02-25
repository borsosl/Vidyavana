// http://localhost:8080/test.html?unit=highlight

/// <reference path="../defs/client.d.ts"/>

import highlight from '../modules/highlight';

$(function()
{
    // wait until other modules load
    test('main', main);
});

function main() {
    let h = highlight.init("pandu|Pāṇḍu fiai");
    ok(h.lowercase('A Bhagavad-gītā úgy, ahogy van') === 'a bhagavad-gītā úgy, ahogy van');
    ok(h.lowercase('Pāṇḍu feleségének, Kuntīnak, vagyis Pṛthānak, a Pāṇḍavák anyjának') === 'pāṇḍu feleségének, kuntīnak, vagyis pṛthānak, a pāṇḍavák anyjának');
    ok(h.lowercase('Īśvara Śrī Caitanya Śaibya Ám Óh') === 'īśvara śrī caitanya śaibya ám óh');
    h.wordArr([['pandu', false, undefined], ['pandu', true, undefined], ['fiai', false, undefined]]);
    ok(h.sought(['pandu', 12, 17]) === true);
    ok(h.sought(['fiai', 12, 17]) === true);
    ok(h.sought(['más', 28, 32]) === false);
    let hi = h.highlightIndexes('<b>Dhṛtarāṣṭra így szólt:&nbsp;Óh, Sañjaya, mit tettek fiaim és Pāṇḍu fiai, miután');
    ok(hi.length === 2 && hi[0][0] === 64 && hi[1][1] === 74);
}
