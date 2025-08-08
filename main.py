import kivy
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.slider import Slider
from kivy.uix.label import Label
from kivy.clock import Clock
import threading
import random
import subprocess
import sys
import os

kivy.require('2.0.0')

class SquashGhosting(App):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        
        # Squash court positions
        self.positions = [
            'left', 'right', 'top-left-corner',
            'bottom-left-corner', 'top-right-corner', 'bottom-right-corner'
        ]
        
        # Training variables
        self.is_training = False
        self.current_set = 0
        self.current_position_index = 0
        self.total_sets = 5
        self.delay_between_sets = 10
        self.current_positions_list = []
        self.scheduled_events = []

    def build(self):
        # Main layout
        main_layout = BoxLayout(orientation='vertical', padding=20, spacing=15)
        
        # Title
        title = Label(
            text='Squash Ghosting Trainer',
            font_size=24,
            size_hint=(1, 0.1),
            color=(1, 1, 1, 1)
        )
        main_layout.add_widget(title)
        
        # Number of sets slider
        sets_layout = BoxLayout(orientation='vertical', size_hint=(1, 0.2))
        self.sets_label = Label(text=f'Number of Sets: {self.total_sets}', font_size=18)
        self.sets_slider = Slider(
            min=5, max=15, value=5, step=1,
            size_hint=(1, 0.5)
        )
        self.sets_slider.bind(value=self.on_sets_slider_change)
        sets_layout.add_widget(self.sets_label)
        sets_layout.add_widget(self.sets_slider)
        main_layout.add_widget(sets_layout)
        
        # Time delay slider
        delay_layout = BoxLayout(orientation='vertical', size_hint=(1, 0.2))
        self.delay_label = Label(text=f'Delay Between Sets (seconds): {self.delay_between_sets}', font_size=18)
        self.delay_slider = Slider(
            min=10, max=30, value=10, step=5,
            size_hint=(1, 0.5)
        )
        self.delay_slider.bind(value=self.on_delay_slider_change)
        delay_layout.add_widget(self.delay_label)
        delay_layout.add_widget(self.delay_slider)
        main_layout.add_widget(delay_layout)
        
        # Status label
        self.status_label = Label(
            text='Ready to start training',
            font_size=16,
            size_hint=(1, 0.1)
        )
        main_layout.add_widget(self.status_label)
        
        # Progress label
        self.progress_label = Label(
            text='',
            font_size=14,
            size_hint=(1, 0.1)
        )
        main_layout.add_widget(self.progress_label)
        
        # Buttons layout
        button_layout = BoxLayout(orientation='horizontal', size_hint=(1, 0.2), spacing=10)
        
        # Start button
        self.start_button = Button(
            text='Start Training',
            font_size=18,
            background_color=(0.2, 0.8, 0.2, 1)
        )
        self.start_button.bind(on_press=self.start_training)
        button_layout.add_widget(self.start_button)
        
        # End button
        self.end_button = Button(
            text='End Training',
            font_size=18,
            background_color=(0.8, 0.2, 0.2, 1),
            disabled=True
        )
        self.end_button.bind(on_press=self.end_training)
        button_layout.add_widget(self.end_button)
        
        main_layout.add_widget(button_layout)
        return main_layout

    def speak_text(self, text):
        """
        Cross-platform text-to-speech using system commands
        This avoids the COM object issues with pyttsx3
        """
        def speak_worker():
            try:
                # Convert position text to more natural speech
                speech_text = text.replace('-', ' ').replace('corner', 'corner')
                
                if sys.platform.startswith('win'):
                    # Windows - use PowerShell's built-in speech synthesis
                    cmd = [
                        'powershell', '-Command',
                        f'Add-Type -AssemblyName System.Speech; '
                        f'$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; '
                        f'$speak.Rate = 2; '
                        f'$speak.Volume = 100; '
                        f'$speak.Speak("{speech_text}"); '
                        f'$speak.Dispose()'
                    ]
                    
                    # Use subprocess with timeout to prevent hanging
                    process = subprocess.Popen(
                        cmd, 
                        stdout=subprocess.PIPE, 
                        stderr=subprocess.PIPE,
                        creationflags=subprocess.CREATE_NO_WINDOW if sys.platform.startswith('win') else 0
                    )
                    
                    try:
                        stdout, stderr = process.communicate(timeout=5)  # 5 second timeout
                        if process.returncode == 0:
                            print(f"Successfully spoke: {speech_text}")
                        else:
                            print(f"TTS Error: {stderr.decode()}")
                    except subprocess.TimeoutExpired:
                        process.kill()
                        print(f"TTS timeout for: {speech_text}")
                        
                elif sys.platform.startswith('darwin'):  # macOS
                    subprocess.run(['say', speech_text], timeout=5)
                    print(f"Successfully spoke: {speech_text}")
                    
                elif sys.platform.startswith('linux'):  # Linux
                    # Try espeak first, then festival
                    try:
                        subprocess.run(['espeak', speech_text], timeout=5, check=True)
                        print(f"Successfully spoke: {speech_text}")
                    except (subprocess.CalledProcessError, FileNotFoundError):
                        try:
                            subprocess.run(['festival', '--tts'], input=speech_text.encode(), timeout=5)
                            print(f"Successfully spoke: {speech_text}")
                        except (subprocess.CalledProcessError, FileNotFoundError):
                            print("No TTS engine available on Linux. Install espeak or festival.")
                elif sys.platform.startswith('android'):
                    # Use Android's native TTS
                    from jnius import autoclass
                    from android.runnable import run_on_ui_thread
                
                    @run_on_ui_thread
                    def android_speak():
                        TTS = autoclass('android.speech.tts.TextToSpeech')
                        Locale = autoclass('java.util.Locale')
                        PythonActivity = autoclass('org.kivy.android.PythonActivity')
                        
                        activity = PythonActivity.mActivity
                        tts = TTS(activity, None)
                        tts.setLanguage(Locale.ENGLISH)
                        tts.speak(speech_text, TTS.QUEUE_FLUSH, None)
                    
                    android_speak()
                            
            except Exception as e:
                print(f"TTS Error for '{text}': {e}")
        
        # Run in daemon thread
        tts_thread = threading.Thread(target=speak_worker, daemon=True)
        tts_thread.start()

    def on_sets_slider_change(self, instance, value):
        """Update number of sets when slider changes"""
        self.total_sets = int(value)
        self.sets_label.text = f'Number of Sets: {self.total_sets}'

    def on_delay_slider_change(self, instance, value):
        """Update delay between sets when slider changes"""
        self.delay_between_sets = int(value)
        self.delay_label.text = f'Delay Between Sets (seconds): {self.delay_between_sets}'

    def start_training(self, instance):
        """Start the ghosting training session"""
        if not self.is_training:
            self.is_training = True
            self.current_set = 1
            self.current_position_index = 0
            
            # Update UI
            self.start_button.disabled = True
            self.end_button.disabled = False
            self.sets_slider.disabled = True
            self.delay_slider.disabled = True
            self.status_label.text = 'Training in progress...'
            
            # Start the first set immediately
            self.start_new_set()

    def start_new_set(self):
        """Start a new set of positions"""
        if not self.is_training:
            return
            
        # Check if we've completed all sets
        if self.current_set > self.total_sets:
            self.complete_training()
            return
        
        # Create shuffled positions for this set
        self.current_positions_list = self.positions.copy()
        random.shuffle(self.current_positions_list)
        self.current_position_index = 0
        
        # Update progress
        self.update_progress_label(f"Set {self.current_set}/{self.total_sets} - Starting...")
        
        # Start speaking the first position after a short delay
        event = Clock.schedule_once(lambda dt: self.speak_next_position(), 1.0)
        self.scheduled_events.append(event)

    def speak_next_position(self):
        """Speak the next position in the current set"""
        if not self.is_training:
            return
        
        # Check if we've completed the current set
        if self.current_position_index >= len(self.current_positions_list):
            self.complete_current_set()
            return
        
        # Get the current position
        position = self.current_positions_list[self.current_position_index]
        self.current_position_index += 1
        
        # Update progress
        self.update_progress_label(
            f"Set {self.current_set}/{self.total_sets} - Position {self.current_position_index}/{len(self.current_positions_list)}: {position.replace('-', ' ').title()}"
        )
        
        # Speak the position
        self.speak_text(position)
        
        # Schedule the next position after 3 seconds
        event = Clock.schedule_once(lambda dt: self.speak_next_position(), 3.0)
        self.scheduled_events.append(event)

    def complete_current_set(self):
        """Handle completion of the current set"""
        if not self.is_training:
            return
        
        print(f"Completed set {self.current_set}")  # Debug output
        
        # Move to next set
        self.current_set += 1
        
        # Check if this was the last set
        if self.current_set > self.total_sets:
            print("All sets completed, calling complete_training")  # Debug output
            self.complete_training()
            return
        
        # Update progress for set completion
        self.update_progress_label(
            f"Set {self.current_set - 1} completed. Next set starting in {self.delay_between_sets} seconds..."
        )
        
        # Schedule the next set after the delay
        event = Clock.schedule_once(lambda dt: self.start_new_set(), self.delay_between_sets)
        self.scheduled_events.append(event)

    def complete_training(self):
        """Handle completion of all training"""
        print("Training completed!")  # Debug output
        
        if self.is_training:  # Only if training wasn't manually stopped
            self.update_progress_label("Training completed! Well done!")
            self.speak_text("Training completed. Well done!")
            
            # Schedule UI reset after a delay
            event = Clock.schedule_once(lambda dt: self.reset_ui(), 4.0)
            self.scheduled_events.append(event)
        else:
            self.reset_ui()

    def end_training(self, instance):
        """End the training session"""
        print("Ending training manually")  # Debug output
        self.is_training = False
        
        # Cancel all scheduled events
        for event in self.scheduled_events:
            try:
                event.cancel()
            except:
                pass
        self.scheduled_events.clear()
        
        # Reset UI immediately
        self.reset_ui()

    def reset_ui(self):
        """Reset UI elements to initial state"""
        print("Resetting UI")  # Debug output
        self.is_training = False
        self.start_button.disabled = False
        self.end_button.disabled = True
        self.sets_slider.disabled = False
        self.delay_slider.disabled = False
        self.status_label.text = 'Ready to start training'
        self.progress_label.text = ''

    def update_progress_label(self, text):
        """Update the progress label text"""
        self.progress_label.text = text

    def on_stop(self):
        """Clean up when app is closed"""
        print("App stopping")  # Debug output
        self.is_training = False
        
        # Cancel all scheduled events
        for event in self.scheduled_events:
            try:
                event.cancel()
            except:
                pass
        self.scheduled_events.clear()

if __name__ == '__main__':
    SquashGhosting().run()
