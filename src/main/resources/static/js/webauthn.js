// webauthn.js

async function registerWebAuthn() {
    try {
        const response = await fetch('/webauthn/register/start', { method: 'POST' });
        const data = await response.json();

        // Construct correct PublicKeyCredentialCreationOptions
        const publicKey = {
            challenge: base64UrlToArrayBuffer(data.challenge),
            rp: {
                name: "Hannie Book Store"
                // id: window.location.hostname // Let browser detect automatically to avoid localhost issues
            },
            user: {
                id: base64UrlToArrayBuffer(data.userId),
                name: data.username,
                displayName: data.displayName
            },
            pubKeyCredParams: [
                { type: "public-key", alg: -7 }, // ES256
                { type: "public-key", alg: -257 } // RS256
            ],
            authenticatorSelection: {
                authenticatorAttachment: "platform",
                userVerification: "preferred", // Relaxed from required
                residentKey: "required",
                requireResidentKey: true
            },
            timeout: 60000,
            attestation: "none"
        };

        const credential = await navigator.credentials.create({ publicKey: publicKey });

        const attestationObject = arrayBufferToBase64Url(credential.response.attestationObject);
        const clientDataJSON = arrayBufferToBase64Url(credential.response.clientDataJSON);
        const rawId = arrayBufferToBase64Url(credential.rawId);

        const finishResponse = await fetch('/webauthn/register/finish', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                credentialId: rawId,
                clientDataJSON: clientDataJSON,
                attestationObject: attestationObject
            })
        });

        if (finishResponse.ok) {
            alert("Đăng ký FaceID/Windows Hello thành công!");
            location.reload();
        } else {
            alert("Đăng ký thất bại.");
        }
    } catch (e) {
        console.error(e);
        alert("Lỗi đăng ký: " + e.message);
    }
}

async function loginWebAuthn() {
    try {
        const response = await fetch('/webauthn/login/start', { method: 'POST' });
        const data = await response.json();

        const publicKey = {
            challenge: base64UrlToArrayBuffer(data.challenge),
            // rpId: window.location.hostname, // Let browser detect
            timeout: 60000,
            userVerification: "preferred" // Relaxed
        };

        const assertion = await navigator.credentials.get({ publicKey: publicKey });

        const authenticatorData = arrayBufferToBase64Url(assertion.response.authenticatorData);
        const clientDataJSON = arrayBufferToBase64Url(assertion.response.clientDataJSON);
        const signature = arrayBufferToBase64Url(assertion.response.signature);
        const rawId = arrayBufferToBase64Url(assertion.rawId);

        const finishResponse = await fetch('/webauthn/login/finish', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                credentialId: rawId,
                authenticatorData: authenticatorData,
                clientDataJSON: clientDataJSON,
                signature: signature
            })
        });

        if (finishResponse.ok) {
            window.location.href = "/";
        } else {
            alert("Đăng nhập thất bại.");
        }

    } catch (e) {
        console.error(e);
        alert("Lỗi đăng nhập: " + e.message);
    }
}

function base64UrlToArrayBuffer(base64url) {
    const padding = '='.repeat((4 - base64url.length % 4) % 4);
    const base64 = (base64url + padding).replace(/\-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray.buffer;
}

function arrayBufferToBase64Url(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}
