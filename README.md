
<h1>Squash Ghosting</h1>
        <p class="lead">A lightweight, spoken-instruction trainer for squash ghosting practice. Configure sets, pacing and spoken cues to power your court movement training—no coach required.</p>
        <div style="margin-top:6px"><span class="badge">v1.0.0</span> <span style="margin-left:8px" class="muted">• Built for focused footwork & conditioning</span></div>

    <section class="hero" style="margin-top:18px">
      <div class="card">
        <h3>Overview</h3>
        <p class="muted">Squash Ghosting helps players improve court coverage using repeatable, spoken instructions. Choose how many sets to run, set the delay between sets, and configure the delay between each spoken instruction. The app handles timing and speech synthesis so you can concentrate on movement.</p>

        <h4 style="margin-top:12px">Why it helps</h4>
        <ul class="features">
          <li>Removes the mental timer; follow clear spoken cues and trust your feet.</li>
          <li>Customizable intensity — shorter delays for conditioning, longer for technique work.</li>
          <li>Accessible — works hands-free with keyboard shortcuts and screen-reader friendly labels.</li>
        </ul>

        <div style="margin-top:12px">
          <h4>Core features</h4>
          <div class="grid-2" style="margin-top:8px">
            <div>
              <ul class="features">
                <li><strong>Sets:</strong> Choose 1–12 sets</li>
                <li><strong>Delay between sets:</strong> Rest time in seconds or minutes</li>
                <li><strong>Delay between instructions:</strong> Control pace of spoken cues</li>
              </ul>
            </div>
            <div>
              <ul class="features">
                <li><strong>Speech synthesis:</strong> Human-friendly voice cues</li>
                <li><strong>Quick controls:</strong> Start / Pause / Stop / Skip</li>
                <li><strong>Config export:</strong> Save & load training profiles</li>
              </ul>
            </div>
          </div>
        </div>

        <h4 style="margin-top:12px">Quick start</h4>
        <ol style="color:#cfe6ff">
          <li>Download or clone the repo.</li>
          <li>Open <code class="inline">index.html</code> in a modern browser (Chrome / Edge).</li>
          <li>Allow microphone/speech (if requested) and set your preferred voice.</li>
          <li>Pick number of sets, set delays, and press <code class="inline">Start</code>.</li>
        </ol>

        <h4 style="margin-top:12px">Example config</h4>
        <pre><code>{
  "sets": 6,
  "delayBetweenSetsSeconds": 90,
  "delayBetweenInstructionsSeconds": 6,
  "voice": "default"
}</code></pre>

        <p class="muted" style="margin-top:10px">Tip: Start with 4–6 sets with 60–90s rest and 6s instruction spacing for mixed technical & conditioning work.</p>

      </div>

      <aside style="display:flex;flex-direction:column;gap:12px">
        <div class="card" style="padding:12px;display:flex;flex-direction:column;gap:8px;align-items:center;justify-content:center">
          <img src="assets/squash-ghosting.jpg" alt="Player ghosting on a squash court — silhouette mid-lunge" class="screenshot" onerror="this.style.display='none'">
          <div style="text-align:center;margin-top:6px;color:var(--muted);font-size:13px">Replace <code class="inline">assets/squash-ghosting.jpg</code> with your photo or GIF</div>
        </div>

        <div class="card">
          <h4>Controls & Shortcuts</h4>
          <ul class="features">
            <li><strong>Space</strong> — Start / Pause</li>
            <li><strong>S</strong> — Stop & Reset</li>
            <li><strong>N</strong> — Skip to next instruction</li>
            <li><strong>Up/Down</strong> — Increase / Decrease instruction delay</li>
          </ul>
        </div>

        <div class="card" style="text-align:center">
          <h4>Download</h4>
          <p class="muted">Want a packaged build? Grab the latest release from <code class="inline">/releases</code> or clone the repository.</p>
          <a class="btn" href="#">Get release</a>
        </div>
      </aside>
    </section>

    <section style="margin-top:18px" class="card">
      <h3>Technical Notes</h3>
      <p class="muted">The app uses the Web Speech API (speechSynthesis) for spoken cues and a small state machine to sequence instructions and sets. It is intentionally front-end only so you can run it locally without installing dependencies. If you want persistent workout history or synced profiles, add a tiny backend (Firebase / Azure Static Web Apps + Functions).</p>

      <h4 style="margin-top:10px">Files of interest</h4>
      <ul class="features">
        <li><code class="inline">index.html</code> — UI and entry point</li>
        <li><code class="inline">/assets</code> — images & media</li>
        <li><code class="inline">/js/ghosting.js</code> — timer + speech logic</li>
        <li><code class="inline">/css/styles.css</code> — optional external styles</li>
      </ul>

      <h4 style="margin-top:10px">Extending the app</h4>
      <p class="muted">Ideas to make it more coach-like:</p>
      <ul class="features">
        <li>Record custom cue sequences (e.g., "drop, front-left, back-right") and cycle through them.</li>
        <li>Add interval modes (Tabata-style) with warmup/cooldown phases.</li>
        <li>Integrate audio cues or a metronome in addition to speech.</li>
      </ul>
    </section>

    <section style="margin-top:18px" class="card">
      <h3>Contributing</h3>
      <p class="muted">All contributions welcome — raise an issue for feature suggestions or bug reports. Prefer small, focused PRs. Use conventional commits for clear changelogs.</p>
      <p style="margin-top:8px"><strong>Example pull request:</strong> Add an optional countdown audio beep before each set starts.</p>

      <h4 style="margin-top:10px">License</h4>
      <p class="muted">MIT — free to use, modify and share. Credit appreciated.</p>
    </section>

    <footer>
      Built with sweat, focus and a little imagination — happy ghosting! • <span class="muted">Squash Ghosting • © 2025</span>
    </footer>

  </div>
</body>
</html>
