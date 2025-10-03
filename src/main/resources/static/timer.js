// Timer functionality for problem practice sessions
class ProblemTimer {
    constructor() {
        this.timer = null;
        this.timeLeft = 45 * 60; // 45 minutes in seconds
        this.timerRunning = false;
        this.problemId = null;
        this.problemStatus = 'PENDING';

        this.initialize();
    }

    initialize() {
        console.log('Problem Timer initialized');

        // Get problem data from data attributes
        const timerCard = document.getElementById('timerCard');
        if (timerCard) {
            this.problemId = timerCard.dataset.problemId;
            this.problemStatus = timerCard.dataset.problemStatus || 'PENDING';
        }

        console.log('Problem ID:', this.problemId);
        console.log('Problem Status:', this.problemStatus);

        this.initializePageState();
        this.bindEvents();
    }

    initializePageState() {
        console.log('Initializing page with status:', this.problemStatus);

        // Hide all states first
        this.hideAllStates();

        // Show appropriate state based on status
        if (this.problemStatus === 'COMPLETED') {
            this.showState('completedState');
        } else if (this.problemStatus === 'IN_PROGRESS') {
            this.showState('inProgressState');
        } else {
            this.showState('pendingState');
        }
    }

    hideAllStates() {
        const states = ['pendingState', 'activeState', 'completedState', 'inProgressState'];
        states.forEach(state => {
            const element = document.getElementById(state);
            if (element) {
                element.style.display = 'none';
            }
        });
    }

    showState(stateId) {
        this.hideAllStates();
        const element = document.getElementById(stateId);
        if (element) {
            element.style.display = 'block';
        }
    }

    bindEvents() {
        // Bind event listeners for buttons that might be added dynamically
        document.addEventListener('click', (e) => {
            if (e.target.closest('[data-action="start-timer"]')) {
                this.startTimer();
            } else if (e.target.closest('[data-action="stop-timer"]')) {
                this.stopTimer();
            } else if (e.target.closest('[data-action="complete-problem"]')) {
                this.completeProblem();
            }
        });
    }

    startTimer() {
        console.log('Start timer clicked');
        if (this.timerRunning) {
            console.log('Timer already running');
            return;
        }

        // Update server status if problem is PENDING
        if (this.problemStatus === 'PENDING') {
            this.updateServerStatus('IN_PROGRESS');
        }

        // Show active state
        this.showState('activeState');
        document.getElementById('timerCard').classList.add('timer-running');

        // Start the countdown
        this.timerRunning = true;
        this.timer = setInterval(() => {
            this.timeLeft--;
            this.updateTimerDisplay();

            if (this.timeLeft <= 0) {
                this.handleTimerComplete();
            }
        }, 1000);

        console.log('Timer started');
    }

    stopTimer() {
        console.log('Stop timer clicked');
        this.clearTimer();

        // Reset timer display
        this.timeLeft = 45 * 60;
        this.updateTimerDisplay();

        // Show appropriate state
        if (this.problemStatus === 'IN_PROGRESS') {
            this.showState('inProgressState');
        } else {
            this.showState('pendingState');
        }

        document.getElementById('timerCard').classList.remove('timer-running');

        console.log('Timer stopped');
    }

    completeProblem() {
        console.log('Complete problem clicked');
        this.clearTimer();

        // Update server status
        this.updateServerStatus('COMPLETED');

        // Update local status and UI
        this.problemStatus = 'COMPLETED';
        this.showState('completedState');
        this.updateStatusBadge('COMPLETED', 'bg-success');

        console.log('Problem marked as completed');
    }

    handleTimerComplete() {
        this.clearTimer();
        document.getElementById('timerDisplay').innerHTML = '<span class="text-danger">Time\'s up!</span>';
        document.getElementById('timerStatus').textContent = 'Session completed!';

        // Auto-complete after 2 seconds
        setTimeout(() => {
            this.completeProblem();
        }, 2000);
    }

    clearTimer() {
        if (this.timer) {
            clearInterval(this.timer);
            this.timerRunning = false;
        }
    }

    updateTimerDisplay() {
        const minutes = Math.floor(this.timeLeft / 60);
        const seconds = this.timeLeft % 60;
        const display = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

        // Update all timer displays
        const displays = document.querySelectorAll('.timer-display');
        displays.forEach(displayElement => {
            if (displayElement.id !== 'timerDisplay' || this.timerRunning) {
                displayElement.textContent = display;
            }
        });

        // Update status text
        this.updateTimerStatus();
    }

    updateTimerStatus() {
        const statusElement = document.getElementById('timerStatus');
        if (!statusElement) return;

        if (this.timeLeft <= 300 && this.timeLeft > 0) {
            statusElement.textContent = 'Almost there! Keep going!';
        } else if (this.timeLeft > 0) {
            statusElement.textContent = 'Time remaining...';
        }
    }

    updateStatusBadge(status, badgeClass) {
        const statusBadge = document.querySelector('[data-status-badge]');
        if (statusBadge) {
            statusBadge.textContent = status;
            statusBadge.className = `badge ${badgeClass}`;
        }
    }

    async updateServerStatus(status) {
        if (!this.problemId) return;

        try {
            const endpoint = status === 'COMPLETED' ? 'complete' : 'start-timer';
            const response = await fetch(`/problems/${this.problemId}/${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                this.problemStatus = status;
                console.log(`Status updated to ${status} on server`);
            } else {
                console.error('Failed to update status on server');
            }
        } catch (error) {
            console.error('Error updating server status:', error);
        }
    }
}

// Initialize timer when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.problemTimer = new ProblemTimer();
});

// Utility functions for inline event handlers (backward compatibility)
window.startTimer = function() {
    if (window.problemTimer) {
        window.problemTimer.startTimer();
    }
};

window.stopTimer = function() {
    if (window.problemTimer) {
        window.problemTimer.stopTimer();
    }
};

window.completeProblem = function() {
    if (window.problemTimer) {
        window.problemTimer.completeProblem();
    }
};