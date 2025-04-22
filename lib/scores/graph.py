import matplotlib.pyplot as plt
import pandas as pd
import sys
import os

def plot_scores(csv_file):
    # Read the CSV file
    scores = pd.read_csv(csv_file, header=None, names=['Score'])
    
    # Create the plot
    plt.figure(figsize=(12, 6))
    plt.plot(scores.index, scores['Score'], linewidth=1)
    
    # Customize the plot
    plt.title('Score Progression Over Time')
    plt.xlabel('Iteration')
    plt.ylabel('Score')
    plt.grid(True, alpha=0.3)
    
    # Add max score line
    max_score = scores['Score'].max()
    plt.axhline(y=max_score, color='r', linestyle='--', alpha=0.5, 
                label=f'Max Score: {max_score}')
    
    # Add final score annotation
    final_score = scores['Score'].iloc[-1]
    plt.annotate(f'Final: {final_score}', 
                xy=(len(scores)-1, final_score),
                xytext=(10, 10), textcoords='offset points')
    
    plt.legend()
    
    # Save the plot
    output_file = os.path.splitext(csv_file)[0] + '.png'
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Plot saved to: {output_file}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python graph.py <csv_file>")
        sys.exit(1)
    
    csv_file = sys.argv[1]
    plot_scores(csv_file)

