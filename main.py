from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.slider import Slider
from kivy.uix.button import Button
from kivy.core.audio import SoundLoader
from kivy.clock import Clock
import pyttsx3

class SquashGhost(App):
    def build(self):
        self.words = ["left", "right", "top left corner","top-right-corner","bottom-left-corner","bottom-right-corner" ]
        self.current_set = 0
        self.total_sets = 5              # default value
        self.delay_bw_sets = 10          # default value for delay between sets

        # Initialize text-to-speech engine
        self.engine = pyttsx3.init()

        # Main layout
        layout = BoxLayout(orientation='vertical', padding=20, spacing=10)

        # Heading for the number of sets slider
        self.heading_label = Label(text="Number of sets: 5", font_size=20)
        layout.add_widget(self.heading_label)

        # Slider (5 to 15) for number of sets
        self.slider = Slider(min=5, max=15, value=5, step=1)
        self.slider.bind(value=self.on_slider_change)
        layout.add_widget(self.slider)

        # Heading for the delay between the sets
        self.heading_label1 = Label(text="Time delay between sets: 10", font_size=20)
        layout.add_widget(self.heading_label1)

        # Slider (10 to 30) for time delay between the sets
        self.slider1 = Slider(min=10, max=30, value=10, step=5)
        self.slider1.bind(value=self.on_slider_change1)
        layout.add_widget(self.slider1)

        # Start button
        start_button = Button(text="Start Reading", background_color=(0, 0, 1, 1), font_size=20)
        start_button.bind(on_press=self.start_reading)
        layout.add_widget(start_button)

        return layout

    def on_slider_change(self, instance, value):
        self.total_sets = int(value)
        self.heading_label.text = f"Number of sets: {self.total_sets}"

    def on_slider_change1(self, instance, value_1):
        self.delay_bw_sets = int(value_1)
        self.heading_label1.text = f"Time delay between sets: {self.delay_bw_sets}"

    def start_reading(self, instance):
        self.current_set = 0
        self.event = Clock.schedule_interval(self.read_next_word, self.delay_bw_sets)

    def read_next_word(self, dt):
        self.current_set += 1

        for word in self.words:
            self.engine.say(word)
        self.engine.runAndWait()

        if self.current_set >= self.total_sets:
            Clock.unschedule(self.event)




if __name__ == "__main__":
    SquashGhost().run()
