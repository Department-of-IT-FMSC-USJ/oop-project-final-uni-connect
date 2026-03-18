async function testSubmit() {
    const email = "it123460@example.com";
    const regNo = "it123460";

    // 1. Register
    console.log("Registering student...");
    const regRes = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            fullName: "Test Underg",
            email: email,
            password: "password123",
            confirmPassword: "password123",
            role: "UNDERGRADUATE",
            department: "IT",
            registrationNumber: regNo,
            yearOfStudy: 2
        })
    });
    const regText = await regRes.text();
    console.log("Register:", regRes.status, regText);

    // 2. Login
    console.log("Logging...");
    const loginRes = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: email,
            password: "password123"
        })
    });
    
    const loginText = await loginRes.text();
    console.log("Login Status:", loginRes.status, loginText);
    if (!loginRes.ok) {
        console.error("Login failed.");
        return;
    }
    
    // AuthController might return a mock string or real JSON depending on my findings
    let token;
    if (loginText.startsWith("Login successful token=")) {
        token = loginText.split('token=')[1];
    } else {
        try {
            const json = JSON.parse(loginText);
            token = json.token || json.accessToken;
        } catch(e) {
            token = loginText;
        }
    }
    console.log("Parsed token:", token);

    // 3. Submit proof
    console.log("Submitting proof...");
    const proofRes = await fetch('http://localhost:8080/api/proofs', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            title: "My Hackathon",
            description: "Won 1st place",
            cpm: "CPM-1234",
            eventDate: "2026-03-18",
            pointCategory: "AWARD",
            proofType: "text/plain",
            proofData: "data:text/plain;base64,SGVsbG8="
        })
    });

    console.log("Submit proof status:", proofRes.status);
    const proofText = await proofRes.text();
    console.log("Submit proof response:", proofText);

    // 4. Get proofs
    const myProofsRes = await fetch('http://localhost:8080/api/proofs/my', {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    console.log("My proofs status:", myProofsRes.status);
    console.log("My proofs response:", await myProofsRes.text());
}

testSubmit().catch(console.error);
