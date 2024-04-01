/* This is free and unencumbered software released into the public domain. */
"use strict";
const CHACHA_ROUNDS  = 20;
//const CHACHA_KEYSIZE = 32;
const CHACHA_KEYSIZE = 1;
//const CHACHA_IVSIZE  = 8;
const CHACHA_IVSIZE  = 1;

/**
 * Create a new ChaCha generator seeded from the given KEY and IV.
 * Both KEY and IV must be ArrayBuffer objects of at least the
 * appropriate byte lengths (CHACHA_KEYSIZE, CHACHA_IVSIZE). Bytes
 * beyond the specified length are ignored.
 *
 * Returns a closure that takes no arguments. This closure, when
 * invoked, returns a 64-byte ArrayBuffer of the next 64 bytes of
 * output. Note: This buffer object is "owned" by the closure, and
 * the same buffer object is always returned, each time filled with
 * fresh output.
 */

function sha1(input) {
    function rotateLeft(n, s) {
        return (n << s) | (n >>> (32 - s));
    }

    function cvtHex(val) {
        let str = '';
        let i;
        let v;

        for (i = 7; i >= 0; i--) {
            v = (val >>> (i * 4)) & 0x0f;
            str += v.toString(16);
        }
        return str;
    }

    function utf8Encode(str) {
        str = str.replace(/\r\n/g, '\n');
        let utf8str = '';

        for (let n = 0; n < str.length; n++) {
            let c = str.charCodeAt(n);

            if (c < 128) {
                utf8str += String.fromCharCode(c);
            } else if ((c > 127) && (c < 2048)) {
                utf8str += String.fromCharCode((c >> 6) | 192);
                utf8str += String.fromCharCode((c & 63) | 128);
            } else {
                utf8str += String.fromCharCode((c >> 12) | 224);
                utf8str += String.fromCharCode(((c >> 6) & 63) | 128);
                utf8str += String.fromCharCode((c & 63) | 128);
            }
        }
        return utf8str;
    }

    let blockstart;
    let i, j;
    let W = new Array(80);
    let H0 = 0x67452301;
    let H1 = 0xEFCDAB89;
    let H2 = 0x98BADCFE;
    let H3 = 0x10325476;
    let H4 = 0xC3D2E1F0;
    let A, B, C, D, E;
    let temp;

    input = utf8Encode(input);

    let inputLen = input.length;

    let wordArray = [];
    for (i = 0; i < inputLen - 3; i += 4) {
        j = input.charCodeAt(i) << 24 | input.charCodeAt(i + 1) << 16 |
            input.charCodeAt(i + 2) << 8 | input.charCodeAt(i + 3);
        wordArray.push(j);
    }

    switch (inputLen % 4) {
        case 0:
            i = 0x080000000;
            break;
        case 1:
            i = input.charCodeAt(inputLen - 1) << 24 | 0x0800000;
            break;
        case 2:
            i = input.charCodeAt(inputLen - 2) << 24 | input.charCodeAt(inputLen - 1) << 16 | 0x08000;
            break;
        case 3:
            i = input.charCodeAt(inputLen - 3) << 24 | input.charCodeAt(inputLen - 2) << 16 | input.charCodeAt(inputLen - 1) << 8 | 0x80;
            break;
    }

    wordArray.push(i);

    while ((wordArray.length % 16) !== 14) {
        wordArray.push(0);
    }

    wordArray.push(inputLen >>> 29);
    wordArray.push((inputLen << 3) & 0x0ffffffff);

    for (blockstart = 0; blockstart < wordArray.length; blockstart += 16) {
        for (i = 0; i < 16; i++) {
            W[i] = wordArray[blockstart + i];
        }
        for (i = 16; i <= 79; i++) {
            W[i] = rotateLeft(W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16], 1);
        }

        A = H0;
        B = H1;
        C = H2;
        D = H3;
        E = H4;

        for (i = 0; i <= 19; i++) {
            temp = (rotateLeft(A, 5) + ((B & C) | (~B & D)) + E + W[i] + 0x5A827999) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = temp;
        }

        for (i = 20; i <= 39; i++) {
            temp = (rotateLeft(A, 5) + (B ^ C ^ D) + E + W[i] + 0x6ED9EBA1) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = temp;
        }

        for (i = 40; i <= 59; i++) {
            temp = (rotateLeft(A, 5) + ((B & C) | (B & D) | (C & D)) + E + W[i] + 0x8F1BBCDC) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = temp;
        }

        for (i = 60; i <= 79; i++) {
            temp = (rotateLeft(A, 5) + (B ^ C ^ D) + E + W[i] + 0xCA62C1D6) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotateLeft(B, 30);
            B = A;
            A = temp;
        }

        H0 = (H0 + A) & 0x0ffffffff;
        H1 = (H1 + B) & 0x0ffffffff;
        H2 = (H2 + C) & 0x0ffffffff;
        H3 = (H3 + D) & 0x0ffffffff;
        H4 = (H4 + E) & 0x0ffffffff;
    }

    let output = cvtHex(H0) + cvtHex(H1) + cvtHex(H2) + cvtHex(H3) + cvtHex(H4);

    return output.toLowerCase();
}

async function sha256(input) {
    const encoder = new TextEncoder();
    const data = encoder.encode(input);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return hashHex;
}

function ChaCha(key_str, iv_str) {
let encoder = new TextEncoder();
let key_uint8Array = encoder.encode(key_str);
let key = key_uint8Array.buffer;
let iv_uint8Array = encoder.encode(iv_str);
let iv = iv_uint8Array.buffer;
   if (key.byteLength < CHACHA_KEYSIZE)
        throw new Error('key too short');
    if (iv.byteLength < CHACHA_IVSIZE)
        throw new Error('IV too short');
    const state = new Uint32Array(16);
    let view = new DataView(key);
    state[ 0] = 0x61707865; // "expand 32-byte k"
    state[ 1] = 0x3320646e; //
    state[ 2] = 0x79622d32; //
    state[ 3] = 0x6b206574; //
    state[ 4] = view.getUint32( 0, true);
    state[ 5] = view.getUint32( 4, true);
    state[ 6] = view.getUint32( 8, true);
    state[ 7] = view.getUint32(12, true);
    state[ 8] = view.getUint32(16, true);
    state[ 9] = view.getUint32(20, true);
    state[10] = view.getUint32(24, true);
    state[11] = view.getUint32(28, true);
    view = new DataView(iv);
    state[14] = view.getUint32( 0, true);
    state[15] = view.getUint32( 4, true);

    /* Generator */
    const output  = new Uint32Array(16);
    const outview = new DataView(output.buffer);
    return function() {
        function quarterround(x, a, b, c, d) {
            function rotate(v, n) { return (v << n) | (v >>> (32 - n)); }
            x[a] += x[b]; x[d] = rotate(x[d] ^ x[a], 16);
            x[c] += x[d]; x[b] = rotate(x[b] ^ x[c], 12);
            x[a] += x[b]; x[d] = rotate(x[d] ^ x[a],  8);
            x[c] += x[d]; x[b] = rotate(x[b] ^ x[c],  7);
        }
        output.set(state);
        for (let i = 0; i < CHACHA_ROUNDS; i += 2) {
            quarterround(output,  0,  4,  8, 12);
            quarterround(output,  1,  5,  9, 13);
            quarterround(output,  2,  6, 10, 14);
            quarterround(output,  3,  7, 11, 15);
            quarterround(output,  0,  5, 10, 15);
            quarterround(output,  1,  6, 11, 12);
            quarterround(output,  2,  7,  8, 13);
            quarterround(output,  3,  4,  9, 14);
        }
        for (let i = 0; i < 16; i++)
            outview.setUint32(i * 4, output[i] + state[i], true);
        state[12]++;
        if (state[12] == 0) {
            state[13]++;
            if (state[13] == 0) {
                /* One zebibyte of output reached! */
                throw new Error('output exhausted');
            }
        }
        return output.buffer;
    }
}
