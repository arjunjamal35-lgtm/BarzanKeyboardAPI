const send = (res, status, data) => {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");
  res.end(JSON.stringify(data));
};

module.exports = async (req, res) => {
  if (req.method !== "POST") {
    return send(res, 405, { error: "POST required" });
  }

  const expectedToken = process.env.KEYBOARD_API_TOKEN;
  const token = req.headers.authorization;

  if (!expectedToken || token !== `Bearer ${expectedToken}`) {
    return send(res, 401, { error: "Unauthorized" });
  }

  try {
    const body = typeof req.body === "string"
      ? JSON.parse(req.body)
      : req.body || {};

    const { action, text, targetLanguage = "en" } = body;

    if (!["correct", "translate"].includes(action) ||
        typeof text !== "string" ||
        !text.trim() ||
        text.length > 4000 ||
        typeof targetLanguage !== "string" ||
        !/^[a-zA-Z-]{2,20}$/.test(targetLanguage)) {
      return send(res, 400, { error: "Invalid request" });
    }

    const instruction = action === "correct"
      ? "Correct spelling and grammar. Preserve the original language and meaning."
      : `Translate the text into language code ${targetLanguage}.`;

    const prompt = `${instruction}\nReturn only the resulting text.\n\n${text}`;

    let result = null;

    if (process.env.GROQ_API_KEY) {
      try {
        const response = await fetch(
          "https://api.groq.com/openai/v1/chat/completions",
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${process.env.GROQ_API_KEY}`,
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              model: "llama-3.3-70b-versatile",
              messages: [
                { role: "user", content: prompt }
              ]
            }),
            signal: AbortSignal.timeout(20000)
          }
        );

        if (!response.ok) {
      console.error("AI provider HTTP status:", response.status);
    }
    if (response.ok) {
          const data = await response.json();
          result = data.choices?.[0]?.message?.content;
        }
      } catch (err) {
      console.error("AI request exception:", err?.name || "Unknown");
    }
    }

    if (!result && process.env.GEMINI_API_KEY) {
      try {
        const response = await fetch(
          "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
          {
            method: "POST",
            headers: {
              "x-goog-api-key": process.env.GEMINI_API_KEY,
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              contents: [{
                parts: [{ text: prompt }]
              }]
            }),
            signal: AbortSignal.timeout(20000)
          }
        );

        if (!response.ok) {
      console.error("AI provider HTTP status:", response.status);
    }
    if (response.ok) {
          const data = await response.json();
          result = data.candidates?.[0]?.content?.parts
            ?.map(p => p.text || "").join("");
        }
      } catch (err) {
      console.error("AI request exception:", err?.name || "Unknown");
    }
    }

    if (!result) {
      return send(res, 503, { error: "AI service unavailable" });
    }

    return send(res, 200, { result: result.trim() });

  } catch (_) {
    return send(res, 400, { error: "Request processing failed" });
  }
};
