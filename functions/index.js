
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Cấu hình thông tin email của bạn
const gmailEmail = functions.config().gmail.email;
// Mật khẩu ứng dụng đã tạo
const gmailPassword = functions.config().gmail.password;

const mailTransport = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

// Hàm tạo và gửi mã OTP qua email
exports.sendOtpOverEmail = functions.https.onCall(async (data, context) => {
  const email = data.email;

  if (!email) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "The function must be called with one argument 'email'.",
    );
  }

  // Tạo mã OTP 4 số ngẫu nhiên
  const otp = Math.floor(1000 + Math.random() * 9000).toString();

  // Lưu OTP vào Firestore với thời gian hết hạn (ví dụ: 5 phút)
  const db = admin.firestore();
  await db.collection("otps").doc(email).set({
    code: otp,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
  });

  // Cấu hình nội dung email
  const mailOptions = {
    from: `"Your App Name" <${gmailEmail}>`,
    to: email,
    subject: `Your Verification Code is ${otp}`,
    text: `Your verification code is: ${otp}. ` +
          `This code will expire in 5 minutes.`,
  };

  try {
    await mailTransport.sendMail(mailOptions);
    console.log(`Verification code sent to ${email}`);
    return {success: true};
  } catch (error) {
    console.error("There was an error while sending the email:", error);
    throw new functions.https.HttpsError("internal", "Could not send email.");
  }
});
